package com.rwael.mmotors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import com.rwael.mmotors.Controllers.Api.ApiAdminOrdersController;
import com.rwael.mmotors.Database.Order;
import com.rwael.mmotors.Database.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderAdminUnitTests
{
	@Mock
	private OrderRepository orders;

	@InjectMocks
	private ApiAdminOrdersController controller;

	private MockHttpServletResponse response;

	@BeforeEach
	void setup()
	{
		response = new MockHttpServletResponse();
	}

	@Test
	void testListInvalidPageSize()
	{
		Pageable pageable = PageRequest.of(0, 21);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> controller.getList(0, 0, 0, response, pageable));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

		verifyNoInteractions(orders);
	}

	@Test
	void testApproveInvalidOrder()
	{
		when(orders.findById(1)).thenReturn(Optional.empty());

		ApiResult result = controller.postApprove(1, true, response);

		assertFalse(result.isSuccess());
		assertEquals("Ce dossier n'existe pas", result.getMessage());

		verify(orders).findById(1);
		verify(orders, never()).save(any());
	}

	@Test
	void testDuplicateApproveOrder()
	{
		Order order = new Order();
		order.setApprovalDate(Timestamp.valueOf(LocalDateTime.now()));

		when(orders.findById(1)).thenReturn(Optional.of(order));

		ApiResult result = controller.postApprove(1, true, response);

		assertFalse(result.isSuccess());
		assertEquals("Ce dossier a déjà été validé", result.getMessage());

		verify(orders).findById(1);
		verify(orders, never()).save(any());
	}

	@Test
	void testApproveOrder()
	{
		Order order = new Order();

		when(orders.findById(1)).thenReturn(Optional.of(order));

		ApiResult result = controller.postApprove(1, true, response);

		assertTrue(result.isSuccess());

		assertTrue(order.isApproved());
		assertNotNull(order.getApprovalDate());

		verify(orders).save(order);
	}

	@Test
	void testRejectOrder()
	{
		Order order = new Order();

		when(orders.findById(1)).thenReturn(Optional.of(order));

		ApiResult result = controller.postApprove(1, false, response);

		assertTrue(result.isSuccess());

		assertFalse(order.isApproved());
		assertNotNull(order.getApprovalDate());

		verify(orders).save(order);
	}

	@Test
	void testApprovalDate()
	{
		Order order = new Order();

		LocalDateTime before = LocalDateTime.now();

		when(orders.findById(1)).thenReturn(Optional.of(order));

		ApiResult result = controller.postApprove(1, true, response);

		LocalDateTime after = LocalDateTime.now();

		LocalDateTime approvalDate = order.getApprovalDate().toLocalDateTime();

		assertTrue(result.isSuccess());

		assertFalse(approvalDate.isBefore(before));
		assertFalse(approvalDate.isAfter(after));
	}
}
