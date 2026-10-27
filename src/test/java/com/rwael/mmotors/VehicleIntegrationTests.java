package com.rwael.mmotors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.rwael.mmotors.Database.File;
import com.rwael.mmotors.Database.FileRepository;
import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VehicleIntegrationTests
{
	@Autowired
	VehicleRepository vehicles;

	@Autowired
	MockMvc mvc;

	Integer v1Id;
	Integer v2Id;

	@BeforeEach
	void setup()
	{
		vehicles.deleteAll();

		Vehicle v = new Vehicle();
		v.setBrand("Sample");
		v.setModel("Vehicle");
		v.setDescription("...");
		v.setAvailability(1);
		v.setPrice(10000);

		v1Id = vehicles.save(v).getId();

		Vehicle v2 = new Vehicle();
		v2.setBrand("Other");
		v2.setModel("Vehicle");
		v2.setDescription("...");
		v2.setAvailability(1);
		v2.setPrice(15000);

		v2Id = vehicles.save(v2).getId();
	}

	@Test
	void testListRequests() throws Exception
	{
		// No query
		mvc.perform(get("/api/vehicles/list")).andExpectAll(
			status().isOk(),
			jsonPath("$.content[0].brand").value("Sample"),
			jsonPath("$.content[1].brand").value("Other")
		);

		// Valid query
		mvc.perform(get("/api/vehicles/list?query=other")).andExpectAll(
			status().isOk(),
			jsonPath("$.content[0].brand").value("Other"),
			jsonPath("$.content.length()").value(1)
		);

		// Empty page
		mvc.perform(get("/api/vehicles/list?page=2")).andExpectAll(
			status().isOk(),
			jsonPath("$.content.length()").value(0)
		);

		// Page too big
		mvc.perform(get("/api/vehicles/list?size=30")).andExpect(status().isBadRequest());
	}

	@Test
	void testSearchRequests() throws Exception
	{
		// No query
		mvc.perform(get("/api/vehicles/search")).andExpectAll(
			status().isOk(),
			jsonPath("$.content[0].brand").value("Sample"),
			jsonPath("$.content[1].brand").value("Other")
		);

		// Valid query
		mvc.perform(get("/api/vehicles/search?query=other")).andExpectAll(
			status().isOk(),
			jsonPath("$.content[0].brand").value("Other"),
			jsonPath("$.content.length()").value(1)
		);

		// Sort Price Ascending
		mvc.perform(get("/api/vehicles/search?sort=1")).andExpectAll(
			status().isOk(),
			jsonPath("$.content[0].price").value(10000),
			jsonPath("$.content[1].price").value(15000)
		);

		// Sort Price Descending
		mvc.perform(get("/api/vehicles/search?sort=2")).andExpectAll(
			status().isOk(),
			jsonPath("$.content[0].price").value(15000),
			jsonPath("$.content[1].price").value(10000)
		);

		// Empty page
		mvc.perform(get("/api/vehicles/search?page=2")).andExpectAll(
			status().isOk(),
			jsonPath("$.content.length()").value(0)
		);

		// Page too big
		mvc.perform(get("/api/vehicles/search?size=30")).andExpect(status().isBadRequest());
	}

	@Test
	void testDetailRequests() throws Exception
	{
		// Valid vehicle
		mvc.perform(get("/api/vehicles/details/" + v1Id)).andExpectAll(
			status().isOk(),
			jsonPath("$.data.brand").value("Sample")
		);

		// Invalid vehicle
		mvc.perform(get("/api/vehicles/details/999")).andExpect(status().isBadRequest());
	}
}
