package com.rwael.mmotors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.rwael.mmotors.Database.FileReferenceRepository;
import com.rwael.mmotors.Database.FileRepository;
import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VehicleAdminIntegrationTests
{
	@Autowired
	VehicleRepository vehicles;

	@Autowired
	FileReferenceRepository refs;

	@Autowired
	FileRepository files;

	@Autowired
	MockMvc mvc;

	@BeforeEach
	void setup()
	{
		vehicles.deleteAll();
		refs.deleteAll();
		files.deleteAll();
	}

	@Test
	void testLoggedOutVehiclePermissions() throws Exception
	{
		mvc.perform(post("/api/admin/vehicles/add").with(csrf()))
			.andExpect(status().isForbidden());

		mvc.perform(post("/api/admin/vehicles/delete").with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{ \"id\": 1 }"))
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"USER"})
	void testUserVehiclePermissions() throws Exception
	{
		mvc.perform(post("/api/admin/vehicles/add")
			.with(csrf())
			.param("brand", "Ford")
			.param("model", "Focus")
			.param("description", "Test vehicle")
			.param("price", "10000")
			.param("availability", "1"))
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testAddVehicle() throws Exception
	{
		mvc.perform(post("/api/admin/vehicles/add")
			.with(csrf())
			.param("brand", "Ford")
			.param("model", "Focus")
			.param("description", "A test vehicle")
			.param("price", "10000")
			.param("availability", "2"))
			.andExpect(status().isOk());

		assertEquals(1, vehicles.count());

		Vehicle vehicle = vehicles.findByBrandAndModel("Ford", "Focus");

		assertNotNull(vehicle);
		assertEquals(10000, vehicle.getPrice());
		assertEquals(2, vehicle.getAvailability());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testDuplicateVehicleCreation() throws Exception
	{
		mvc.perform(post("/api/admin/vehicles/add")
			.with(csrf())
			.param("brand", "Ford")
			.param("model", "Focus")
			.param("description", "First vehicle")
			.param("price", "10000")
			.param("availability", "1"))
			.andExpect(status().isOk());

		mvc.perform(post("/api/admin/vehicles/add")
			.with(csrf())
			.param("brand", "Ford")
			.param("model", "Focus")
			.param("description", "Duplicate vehicle")
			.param("price", "12000")
			.param("availability", "3"))
			.andExpect(status().isBadRequest());

		assertEquals(1, vehicles.count());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testInvalidVehicleCreation() throws Exception
	{
		// Missing brand
		mvc.perform(post("/api/admin/vehicles/add")
			.with(csrf())
			.param("brand", "")
			.param("model", "Focus")
			.param("description", "Test vehicle")
			.param("price", "10000")
			.param("availability", "1"))
			.andExpect(status().isBadRequest());

		// Missing model
		mvc.perform(post("/api/admin/vehicles/add")
			.with(csrf())
			.param("brand", "Ford")
			.param("model", "")
			.param("description", "Test vehicle")
			.param("price", "10000")
			.param("availability", "1"))
			.andExpect(status().isBadRequest());

		// Invalid price
		mvc.perform(post("/api/admin/vehicles/add")
			.with(csrf())
			.param("brand", "Ford")
			.param("model", "Focus")
			.param("description", "Test vehicle")
			.param("price", "0")
			.param("availability", "1"))
			.andExpect(status().isBadRequest());

		// Invalid availability
		mvc.perform(post("/api/admin/vehicles/add")
			.with(csrf())
			.param("brand", "Ford")
			.param("model", "Focus")
			.param("description", "Test vehicle")
			.param("price", "10000")
			.param("availability", "-1"))
			.andExpect(status().isBadRequest());

		assertEquals(0, vehicles.count());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testEditVehicle() throws Exception
	{
		Vehicle vehicle = new Vehicle();
		vehicle.setBrand("Ford");
		vehicle.setModel("Focus");
		vehicle.setDescription("Original description");
		vehicle.setPrice(10000);
		vehicle.setAvailability(1);
		vehicle = vehicles.save(vehicle);

		mvc.perform(post("/api/admin/vehicles/edit")
			.with(csrf())
			.param("id", vehicle.getId().toString())
			.param("brand", "Ford")
			.param("model", "Focus")
			.param("description", "Updated description")
			.param("price", "15000")
			.param("availability", "3"))
			.andExpect(status().isOk());

		Vehicle updated = vehicles.findById(vehicle.getId()).orElseThrow();

		assertEquals("Updated description", updated.getDescription());
		assertEquals(15000, updated.getPrice());
		assertEquals(3, updated.getAvailability());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testEditNonexistentVehicle() throws Exception
	{
		mvc.perform(post("/api/admin/vehicles/edit")
			.with(csrf())
			.param("id", "999999")
			.param("description", "Updated description"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testDeleteVehicle() throws Exception
	{
		Vehicle vehicle = new Vehicle();
		vehicle.setBrand("Ford");
		vehicle.setModel("Focus");
		vehicle.setDescription("Test vehicle");
		vehicle.setPrice(10000);
		vehicle.setAvailability(1);
		vehicle = vehicles.save(vehicle);

		mvc.perform(post("/api/admin/vehicles/delete")
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{ \"id\": %d }".formatted(vehicle.getId())))
			.andExpect(status().isOk());

		assertFalse(vehicles.existsById(vehicle.getId()));
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testDeleteNonexistentVehicle() throws Exception
	{
		mvc.perform(post("/api/admin/vehicles/delete")
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{ \"id\": 999999 }"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@Transactional
	@WithMockUser(roles = {"ADMIN"})
	void testUploadValidThumbnail() throws Exception
	{
		Vehicle vehicle = new Vehicle();
		vehicle.setBrand("Ford");
		vehicle.setModel("Focus");
		vehicle.setDescription("Test vehicle");
		vehicle.setPrice(10000);
		vehicle.setAvailability(1);
		vehicle = vehicles.save(vehicle);

		MockMultipartFile thumbnail = new MockMultipartFile(
			"files",
			"thumbnail.png",
			"image/png",
			new byte[] {1, 2, 3}
		);

		mvc.perform(multipart("/api/admin/vehicles/edit")
			.file(thumbnail)
			.param("id", vehicle.getId().toString())
			.with(csrf()))
			.andExpect(status().isOk());

		assertEquals(1, vehicle.getThumbs().size());
		assertArrayEquals(new byte[]{1, 2, 3}, vehicle.getThumbs().get(0).getFile().getContent());
	}

	@Test
	@Transactional
	@WithMockUser(roles = {"ADMIN"})
	void testUploadMultipleThumbnails() throws Exception
	{
		Vehicle vehicle = new Vehicle();
		vehicle.setBrand("Ford");
		vehicle.setModel("Focus");
		vehicle.setDescription("Test vehicle");
		vehicle.setPrice(10000);
		vehicle.setAvailability(1);
		vehicle = vehicles.save(vehicle);

		MockMultipartFile first = new MockMultipartFile(
			"files",
			"first.png",
			"image/png",
			new byte[] {1, 2, 3}
		);

		MockMultipartFile second = new MockMultipartFile(
			"files",
			"second.jpg",
			"image/jpeg",
			new byte[] {4, 5, 6}
		);

		mvc.perform(multipart("/api/admin/vehicles/edit")
			.file(first)
			.file(second)
			.param("id", vehicle.getId().toString())
			.with(csrf()))
			.andExpect(status().isOk());

		assertEquals(2, vehicle.getThumbs().size());
		assertArrayEquals(new byte[]{1, 2, 3}, vehicle.getThumbs().get(0).getFile().getContent());
		assertArrayEquals(new byte[]{4, 5, 6}, vehicle.getThumbs().get(1).getFile().getContent());
	}

	@Test
	@Transactional
	@WithMockUser(roles = {"ADMIN"})
	void testUploadInvalidThumbnail() throws Exception
	{
		Vehicle vehicle = new Vehicle();
		vehicle.setBrand("Ford");
		vehicle.setModel("Focus");
		vehicle.setDescription("Test vehicle");
		vehicle.setPrice(10000);
		vehicle.setAvailability(1);
		vehicle = vehicles.save(vehicle);

		MockMultipartFile thumbnail = new MockMultipartFile(
			"files",
			"document.pdf",
			"application/pdf",
			new byte[] {1, 2, 3}
		);

		mvc.perform(multipart("/api/admin/vehicles/edit")
			.file(thumbnail)
			.param("id", vehicle.getId().toString())
			.with(csrf()))
			.andExpectAll(
				status().isBadRequest(),
				jsonPath("$.message").value("Les fichiers doivent être au format PNG, JPG ou JPEG")
			);

		assertEquals(0, vehicle.getThumbs().size());
	}

	@Test
	@Transactional
	@WithMockUser(roles = {"ADMIN"})
	void testUploadMultipleThumbnailsIncludingInvalid() throws Exception
	{
		Vehicle vehicle = new Vehicle();
		vehicle.setBrand("Ford");
		vehicle.setModel("Focus");
		vehicle.setDescription("Test vehicle");
		vehicle.setPrice(10000);
		vehicle.setAvailability(1);
		vehicle = vehicles.save(vehicle);

		MockMultipartFile valid = new MockMultipartFile(
			"files",
			"thumbnail.png",
			"image/png",
			new byte[] {1, 2, 3}
		);

		MockMultipartFile invalid = new MockMultipartFile(
			"files",
			"document.pdf",
			"application/pdf",
			new byte[] {4, 5, 6}
		);

		mvc.perform(multipart("/api/admin/vehicles/edit")
			.file(valid)
			.file(invalid)
			.param("id", vehicle.getId().toString())
			.with(csrf()))
			.andExpectAll(
				status().isBadRequest(),
				jsonPath("$.message").value("Les fichiers doivent être au format PNG, JPG ou JPEG")
			);

		assertEquals(0, vehicle.getThumbs().size());
		assertEquals(0, files.count());
	}

	@Test
	@Transactional
	@WithMockUser(roles = {"ADMIN"})
	void testDeleteThumbnail() throws Exception
	{
		Vehicle vehicle = new Vehicle();
		vehicle.setBrand("Ford");
		vehicle.setModel("Focus");
		vehicle.setDescription("Test vehicle");
		vehicle.setPrice(10000);
		vehicle.setAvailability(1);
		vehicle = vehicles.save(vehicle);

		MockMultipartFile thumbnail = new MockMultipartFile(
			"files",
			"thumbnail.png",
			"image/png",
			new byte[] {1, 2, 3}
		);

		mvc.perform(multipart("/api/admin/vehicles/edit")
			.file(thumbnail)
			.param("id", vehicle.getId().toString())
			.with(csrf()))
			.andExpect(status().isOk());

		assertEquals(1, vehicle.getThumbs().size());

		vehicle = vehicles.findById(vehicle.getId()).orElseThrow();

		Integer refId = vehicle.getThumbs().get(0).getId();
		Integer vehicleId = vehicle.getId();

		mvc.perform(post("/api/admin/vehicles/delete-thumbs")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{
							"vehicleId": %d,
							"refId": %d
					}
					""".formatted(vehicleId, refId))
			.with(csrf()))
			.andExpect(status().isOk());

		assertEquals(0, vehicle.getThumbs().size());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	void testDeleteNonexistentThumbnail() throws Exception
	{
		Vehicle vehicle = new Vehicle();
		vehicle.setBrand("Ford");
		vehicle.setModel("Focus");
		vehicle.setDescription("Test vehicle");
		vehicle.setPrice(10000);
		vehicle.setAvailability(1);
		vehicle = vehicles.save(vehicle);

		mvc.perform(post("/api/admin/vehicles/delete-thumbs")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
				{
					"vehicleId": %d,
					"refId": 999999
				}
				""".formatted(vehicle.getId()))
			.with(csrf()))
			.andExpectAll(
				status().isBadRequest(),
				jsonPath("$.message").value("Cette image n'existe pas")
			);
	}
}
