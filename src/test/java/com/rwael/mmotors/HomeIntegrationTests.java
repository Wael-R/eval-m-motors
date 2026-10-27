package com.rwael.mmotors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
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
public class HomeIntegrationTests
{
	@Autowired 
	FileRepository files;

	@Autowired
	VehicleRepository vehicles;

	@Autowired
	MockMvc mvc;

	@BeforeEach
	void setup()
	{
		files.deleteAll();
	}

	@Test
	void testGetContent() throws Exception
	{
		// Public
		File file = new File();
		file.setContent(new byte[]{1, 2, 3});
		file.setExtension("png");
		file = files.save(file);

		mvc.perform(get("/content/" + file.getHash() + ".png")).andExpect(status().isOk());

		// Private
		file = new File();
		file.setContent(new byte[]{4, 5, 6});
		file.setExtension("png");
		file.setSecure(true);
		file = files.save(file);

		mvc.perform(get("/content/" + file.getHash() + ".png")).andExpect(status().isForbidden());

		// Invalid
		mvc.perform(get("/content/0.png")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testGetPrivateContentAsAdmin() throws Exception
	{
		File file = new File();
		file.setContent(new byte[]{1, 2, 3});
		file.setExtension("png");
		file.setSecure(true);
		file = files.save(file);

		mvc.perform(get("/content/" + file.getHash() + ".png")).andExpect(status().isOk());
	}

	@Test
	void testGetVehicleDetails() throws Exception
	{
		Vehicle v = new Vehicle();
		v.setBrand("Ford");
		v.setModel("Focus");
		v.setDescription("Test vehicle");
		v.setPrice(10000);
		v.setAvailability(1);

		v = vehicles.save(v);

		Vehicle v2 = new Vehicle();
		v2.setBrand("Citroen");
		v2.setModel("C3");
		v2.setDescription("Unavailable vehicle");
		v2.setPrice(10000);
		v2.setAvailability(0);

		v2 = vehicles.save(v2);

		mvc.perform(get("/vehicles/" + v.getId())).andExpect(status().isOk());
		mvc.perform(get("/vehicles/" + v2.getId())).andExpect(status().isNotFound());
		mvc.perform(get("/vehicles/999")).andExpectAll(status().isNotFound());
	}
}
