package com.rwael.mmotors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.rwael.mmotors.Database.FileReference;
import com.rwael.mmotors.Database.FileReferenceRepository;
import com.rwael.mmotors.Database.FileRepository;
import com.rwael.mmotors.Database.FileService;
import com.rwael.mmotors.Database.Order;
import com.rwael.mmotors.Database.OrderRepository;
import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import tools.jackson.databind.ObjectMapper;

import com.rwael.mmotors.Database.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiAdminOrdersIntegrationTests
{
	@Autowired
	private FileRepository fileRepo;

	@Autowired
	private FileReferenceRepository fileRefRepo;

	@Autowired
	private FileService files;

	@Autowired
	private OrderRepository orders;

	@Autowired
	private VehicleRepository vehicles;

	@Autowired
	private UserRepository users;

	@Autowired
	private MockMvc mvc;

	private User user;

	private Vehicle saleVehicle;
	private Vehicle rentalVehicle;

	private Order oldestOrder;
	private Order newestOrder;

	private Vehicle createVehicle(boolean forRenting)
	{
		Vehicle vehicle = new Vehicle();
		vehicle.setBrand("Test brand");
		vehicle.setModel(forRenting ? "Rental model" : "Sale model");
		vehicle.setDescription("Test vehicle");
		vehicle.setPrice(10000);
		vehicle.setAvailability(5);
		vehicle.setForRenting(forRenting);

		return vehicles.save(vehicle);
	}

	private Order createOrder(Vehicle vehicle, boolean approved, Timestamp approvalDate, Timestamp date)
	{
		Order order = new Order();

		order.setVehicle(vehicle);
		order.setApproved(approved);
		order.setApprovalDate(approvalDate);
		order.setDate(date);

		byte[] content = ("order " + date.getTime()).getBytes(java.nio.charset.StandardCharsets.UTF_8);

		FileReference reference = files.getReference(content, "txt", user);

		reference.getFile().setOwner(user);
		reference.getFile().setSecure(true);

		return orders.save(order);
	}

	private String toISO(Timestamp time)
	{
		ObjectMapper mapper = new ObjectMapper();
		String str = mapper.writeValueAsString(time);
		return str.substring(1, str.length() - 1);
	}

	@BeforeEach
	void setup()
	{
		orders.deleteAll();
		vehicles.deleteAll();
		users.deleteAll();
		fileRefRepo.deleteAll();
		fileRepo.deleteAll();

		saleVehicle = createVehicle(false);
		rentalVehicle = createVehicle(true);

		User u = new User();
    u.setEmail("admin@test.com");
    u.setPassword("password");
    u.setFirstName("Test");
    u.setLastName("Admin");
    u.setAdministrator(true);

		user = users.save(u);

		// Pending sale
		oldestOrder = createOrder(saleVehicle, false, null, Timestamp.valueOf("2026-01-01 10:00:00"));

		// Approved sale
		createOrder(saleVehicle, true, Timestamp.valueOf("2026-01-02 10:00:00"), Timestamp.valueOf("2026-01-02 09:00:00"));

		// Rejected rental
		createOrder(rentalVehicle, false, Timestamp.valueOf("2026-01-03 10:00:00"), Timestamp.valueOf("2026-01-03 09:00:00"));

		// Pending rental
		newestOrder = createOrder(rentalVehicle, false, null, Timestamp.valueOf("2026-01-04 10:00:00"));
	}

	@Test
	@WithMockUser(roles = "USER")
	void testListNonAdmin() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list"))
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListDefault() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.totalElements").value(2));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListApproved() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.totalElements").value(1))
			.andExpect(jsonPath("$.content[0].approved").value(true));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListRejected() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.totalElements").value(1))
			.andExpect(jsonPath("$.content[0].approved").value(false));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListAll() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.totalElements").value(4));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListSaleOnly() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "3")
			.param("filterType", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.totalElements").value(2))
			.andExpect(jsonPath("$.content[*].vehicle.forRenting").value(Matchers.everyItem(Matchers.is(false))));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListRentOnly() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "3")
			.param("filterType", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.totalElements").value(2))
			.andExpect(jsonPath("$.content[*].vehicle.forRenting").value(Matchers.everyItem(Matchers.is(true))));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListCombinedFilters() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "0")
			.param("filterType", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page.totalElements").value(1))
			.andExpect(jsonPath("$.content[0].vehicle.forRenting").value(true))
			.andExpect(jsonPath("$.content[0].approvalDate").doesNotExist());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListSortAscending() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "3")
			.param("sort", "0"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].date").value(toISO(oldestOrder.getDate())));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListSortDescending() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "3")
			.param("sort", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].date").value(toISO(newestOrder.getDate())));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListPageSize() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "3")
			.param("size", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.page.totalElements").value(4))
			.andExpect(jsonPath("$.page.totalPages").value(2));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListPageNumber() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("filter", "3")
			.param("size", "2")
			.param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.page.number").value(1));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testListInvalidPageSize() throws Exception
	{
		mvc.perform(get("/api/admin/orders/list")
			.param("size", "21"))
			.andExpect(status().isBadRequest());
	}
}