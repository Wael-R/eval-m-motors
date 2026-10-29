package com.rwael.mmotors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import com.rwael.mmotors.Controllers.Api.ApiPrivateController;
import com.rwael.mmotors.Database.File;
import com.rwael.mmotors.Database.FileReference;
import com.rwael.mmotors.Database.FileService;
import com.rwael.mmotors.Database.Order;
import com.rwael.mmotors.Database.OrderRepository;
import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.UserRepository;
import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class OrderUnitTests
{
	@Mock
	OrderRepository orders;

	@Mock
	VehicleRepository vehicles;

	@Mock
	UserRepository users;

	@Mock
	FileService files;

	@Mock
	HttpServletResponse response;

	@Mock
	Authentication auth;

	@InjectMocks
	ApiPrivateController controller;

	Vehicle vehicle;
	User user;
	FileReference reference;
	MockMultipartFile upload;

	@BeforeEach
	void setup()
	{
		vehicle = new Vehicle();
		vehicle.setId(1);
		vehicle.setBrand("Ford");
		vehicle.setModel("Focus");
		vehicle.setAvailability(2);
		vehicle.setPrice(15000);

		user = new User();
		user.setEmail("a@b.c");

		upload = new MockMultipartFile(
			"files",
			"order.pdf",
			"application/pdf",
			new byte[] {1, 2, 3}
		);

		File f = new File();
		f.setContent(new byte[] {1, 2, 3});
		f.setExtension("pdf");

		reference = new FileReference();
		reference.setFile(f);
	}

	@Test
	void testOrderNoFiles() throws IOException
	{
		controller.postOrder(null, 1, response, auth);

		verify(orders, never()).save(any(Order.class));
		verify(files, never()).getReference(any(MultipartFile.class));
	}

	@Test
	void testOrderEmptyFiles()
	{
		controller.postOrder(new MultipartFile[0], 1, response, auth);

		verify(orders, never()).save(any(Order.class));
	}

	@Test
	void testOrderMultipleFiles() throws IOException
	{
		MultipartFile secondFile = new MockMultipartFile(
			"files",
			"another.pdf",
			"application/pdf",
			new byte[] {4, 5, 6}
		);

		controller.postOrder(new MultipartFile[]{upload, secondFile}, 1, response, auth);

		verify(orders, never()).save(any(Order.class));
		verify(files, never()).getReference(any(MultipartFile.class));
	}

	@Test
	void testOrderInvalidVehicle() throws IOException
	{
		when(vehicles.findById(1)).thenReturn(Optional.empty());

		controller.postOrder(new MultipartFile[]{upload}, 1, response, auth);

		verify(orders, never()).save(any(Order.class));
		verify(files, never()).getReference(any(MultipartFile.class));
	}

	@Test
	void testOrderUnavailableVehicle() throws IOException
	{
		vehicle.setAvailability(0);

		when(vehicles.findById(1)).thenReturn(Optional.of(vehicle));

		controller.postOrder(new MultipartFile[]{upload}, 1, response, auth);

		verify(orders, never()).save(any(Order.class));
		verify(files, never()).getReference(any(MultipartFile.class));
	}

	@Test
	void testOrderForNegativeAvailability()
	{
		vehicle.setAvailability(-1);

		when(vehicles.findById(1)).thenReturn(Optional.of(vehicle));

		controller.postOrder(new MultipartFile[]{upload}, 1, response, auth);

		verify(orders, never()).save(any(Order.class));
	}

	@Test
	void testOrderIOException() throws Exception
	{
		when(vehicles.findById(1)).thenReturn(Optional.of(vehicle));
		when(auth.getName()).thenReturn("a@b.c");
		when(users.findByEmail("a@b.c")).thenReturn(user);
		when(files.getReference(any(MultipartFile.class))).thenThrow(new IOException("Write error"));

		controller.postOrder(new MultipartFile[]{upload}, 1, response, auth);

		verify(orders, never()).save(any(Order.class));
	}

	@Test
	void testValidOrder() throws Exception
	{
		when(vehicles.findById(1)).thenReturn(Optional.of(vehicle));
		when(auth.getName()).thenReturn("a@b.c");
		when(users.findByEmail("a@b.c")).thenReturn(user);
		when(files.getReference(any(MultipartFile.class))).thenReturn(reference);

		controller.postOrder(new MultipartFile[]{upload}, 1, response, auth);

		ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
		verify(orders).save(captor.capture());

		Order saved = captor.getValue();

		assertEquals(user, saved.getUser());
		assertEquals(vehicle, saved.getVehicle());
		assertEquals(reference, saved.getForm());
		assertFalse(saved.isApproved());
		assertNull(saved.getApprovalDate());
		assertTrue(saved.getDate() != null);

		verify(files).getReference(upload);
	}

	@Test
	void testUploadedFileSecure() throws Exception
	{
		when(vehicles.findById(1)).thenReturn(Optional.of(vehicle));
		when(auth.getName()).thenReturn("a@b.c");
		when(users.findByEmail("a@b.c")).thenReturn(user);
		when(files.getReference(any(MultipartFile.class))).thenReturn(reference);

		controller.postOrder(new MultipartFile[]{upload}, 1, response, auth);

		assertEquals(user, reference.getFile().getOwner());
		assertTrue(reference.getFile().isSecure());

		verify(orders).save(any(Order.class));
	}
}