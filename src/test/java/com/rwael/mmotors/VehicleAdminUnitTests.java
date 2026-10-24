package com.rwael.mmotors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.rwael.mmotors.Controllers.Api.ApiAdminVehiclesController;
import com.rwael.mmotors.Controllers.Api.ApiAdminVehiclesController.ThumbData;
import com.rwael.mmotors.Database.FileReference;
import com.rwael.mmotors.Database.FileService;
import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class VehicleAdminUnitTests
{
	@Mock
	VehicleRepository vehicles;

	@Mock
	HttpServletResponse response;

	@Mock
	FileService files;

	@InjectMocks
	ApiAdminVehiclesController controller;

	Vehicle sampleVehicle;

	private Vehicle createVehicle(String brand, String model) {
		Vehicle v = new Vehicle();
		v.setBrand(brand);
		v.setModel(model);
		v.setDescription("Test desc");
		v.setPrice(10000);
		v.setAvailability(1);
		v.setThumbs(new ArrayList<>());

		return v;
	}

	@BeforeEach
	void setup()
	{
		sampleVehicle = new Vehicle();
		sampleVehicle.setId(1);
		sampleVehicle.setBrand("Ford");
		sampleVehicle.setModel("Focus");
		sampleVehicle.setDescription("Sample desc");
		sampleVehicle.setPrice(15000);
		sampleVehicle.setAvailability(5);
		sampleVehicle.setThumbs(new ArrayList<>());
	}

	@Test
	void testAddNewVehicle()
	{
		Vehicle vehicle = createVehicle("Citroen", "C3");

		when(vehicles.findByBrandAndModel("Citroen", "C3")).thenReturn(null);
		when(vehicles.save(vehicle)).thenReturn(vehicle);

		ApiResult result = controller.postAddVehicle(vehicle, null, response);

		assertTrue(result.isSuccess());

		verify(vehicles).findByBrandAndModel("Citroen", "C3");
		verify(vehicles).save(vehicle);
	}

	@Test
	void testAddDuplicateVehicle()
	{
		Vehicle vehicle = createVehicle("Ford", "Focus");

		when(vehicles.findByBrandAndModel("Ford", "Focus")).thenReturn(sampleVehicle);

		ApiResult result = controller.postAddVehicle(vehicle, null, response);

		assertFalse(result.isSuccess());
		assertEquals("Un véhicule existe déjà avec la même marque et modèle", result.getMessage());

		verify(vehicles, never()).save(any(Vehicle.class));
	}

	@Test
	void testEditUpdateModelAndPrice()
	{
		Vehicle submitted = new Vehicle();
		submitted.setId(1);
		submitted.setModel("Fiesta");
		submitted.setPrice(18000);

		when(vehicles.findById(1)).thenReturn(Optional.of(sampleVehicle));
		when(vehicles.save(sampleVehicle)).thenReturn(sampleVehicle);

		ApiResult result = controller.postEditVehicle(submitted, null, response);

		assertTrue(result.isSuccess());

		assertEquals("Ford", sampleVehicle.getBrand());
		assertEquals("Fiesta", sampleVehicle.getModel());
		assertEquals("Sample desc", sampleVehicle.getDescription());
		assertEquals(18000, sampleVehicle.getPrice());
		assertEquals(5, sampleVehicle.getAvailability());

		verify(vehicles).save(sampleVehicle);
	}

	@Test
	void testEditInvalidVehicle()
	{
		Vehicle submitted = new Vehicle();
		submitted.setId(999);

		when(vehicles.findById(999)).thenReturn(Optional.empty());

		ApiResult result = controller.postEditVehicle(submitted, null, response);

		assertFalse(result.isSuccess());
		assertEquals("Aucun véhicule avec cet identifiant", result.getMessage());

		verify(vehicles, never()).save(any(Vehicle.class));
	}

	@Test
	void testEditDuplicateVehicle()
	{
		Vehicle submitted = new Vehicle();
		submitted.setId(1);
		submitted.setBrand("Citroen");
		submitted.setModel("C3");

		when(vehicles.findById(1)).thenReturn(Optional.of(sampleVehicle));
		when(vehicles.save(sampleVehicle)).thenThrow(new DataIntegrityViolationException("Duplicate vehicle"));

		ApiResult result = controller.postEditVehicle(submitted, null, response);

		assertFalse(result.isSuccess());
		assertEquals("Un véhicule existe déjà avec la même marque et modèle", result.getMessage());

		verify(vehicles).save(sampleVehicle);
	}

	@Test
	void testDeleteVehicle()
	{
		when(vehicles.findById(1)).thenReturn(Optional.of(sampleVehicle));

		ApiResult result = controller.postDeleteVehicle(sampleVehicle, response);

		assertTrue(result.isSuccess());

		verify(vehicles).delete(sampleVehicle);
	}

	@Test
	void testDeleteInvalidVehicle()
	{
		Vehicle submitted = new Vehicle();
		submitted.setId(999);

		when(vehicles.findById(999)).thenReturn(Optional.empty());

		ApiResult result = controller.postDeleteVehicle(submitted, response);

		assertFalse(result.isSuccess());
		assertEquals("Aucun véhicule avec cet identifiant", result.getMessage());

		verify(vehicles, never()).delete(any(Vehicle.class));
	}

	@Test
	void testDeleteThumbnail()
	{
		FileReference reference = mock(FileReference.class);

		when(reference.getId()).thenReturn(10);

		sampleVehicle.getThumbs().add(reference);

		when(vehicles.findById(1)).thenReturn(Optional.of(sampleVehicle));
		when(vehicles.save(sampleVehicle)).thenReturn(sampleVehicle);

		ThumbData thumb = new ThumbData(1, 10);

		ApiResult result = controller.postDeleteVehicleThumbs(thumb, response);

		assertTrue(result.isSuccess());
		assertTrue(sampleVehicle.getThumbs().isEmpty());

		verify(vehicles).save(sampleVehicle);
	}

	@Test
	void testDeleteInvalidThumbnail()
	{
		when(vehicles.findById(1)).thenReturn(Optional.of(sampleVehicle));

		ThumbData thumb = new ThumbData(1, 999);

		ApiResult result = controller.postDeleteVehicleThumbs(thumb, response);

		assertFalse(result.isSuccess());
		assertEquals("Cette image n'existe pas", result.getMessage());

		verify(vehicles, never()).save(any(Vehicle.class));
	}

	@Test
	void testDeleteThumbnailInvalidVehicle()
	{
		when(vehicles.findById(999)).thenReturn(Optional.empty());

		ThumbData thumb = new ThumbData(999, 10);

		ApiResult result = controller.postDeleteVehicleThumbs(thumb, response);

		assertFalse(result.isSuccess());
		assertEquals("Aucun véhicule avec cet identifiant", result.getMessage());

		verify(vehicles, never()).save(any(Vehicle.class));
	}

	@Test
	void testAddThumbnail() throws Exception
	{
		byte[] contents = {1, 2, 3};

		MultipartFile file = new MockMultipartFile(
			"files",
			"thumb.png",
			"image/png",
			contents
		);

		FileReference reference = mock(FileReference.class);

		when(files.getReference(any(byte[].class), eq("png"))).thenReturn(reference);

		String result = controller.addVehicleThumbs(sampleVehicle, new MultipartFile[]{file});

		assertNull(result);
		assertEquals(1, sampleVehicle.getThumbs().size());
		assertSame(reference, sampleVehicle.getThumbs().get(0));

		verify(files).getReference(contents, "png");
	}

	@Test
	void testAddInvalidThumbnail() throws Exception
	{
		MultipartFile file = new MockMultipartFile(
			"files",
			"document.pdf",
			"application/pdf",
			new byte[]{1, 2, 3}
		);

		String result = controller.addVehicleThumbs(sampleVehicle, new MultipartFile[]{file});

		assertNotNull(result);
		assertEquals("Les fichiers doivent être au format PNG, JPG ou JPEG", result);

		verify(files, never()).getReference(any(byte[].class), anyString());
	}
}