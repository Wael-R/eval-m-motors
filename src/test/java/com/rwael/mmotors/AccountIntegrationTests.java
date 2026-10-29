package com.rwael.mmotors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.UserRepository;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountIntegrationTests
{
	@Autowired
	UserRepository users;

	@Autowired
	MockMvc mvc;

	@BeforeEach
	void setup()
	{
		users.deleteAll();
	}

	@Test
	void testLoggedOutRoutePermissions() throws Exception
	{
		mvc.perform(get("/")).andExpect(status().isOk());
		mvc.perform(get("/private/")).andExpect(status().isForbidden());
		mvc.perform(get("/admin/")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"USER"})
	void testLoggedInRoutePermissions() throws Exception
	{
		mvc.perform(get("/")).andExpect(status().isOk());
		mvc.perform(get("/private/")).andExpect(status().isOk());
		mvc.perform(get("/admin/")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = {"USER", "ADMIN"})
	void testAdminRoutePermissions() throws Exception
	{
		mvc.perform(get("/")).andExpect(status().isOk());
		mvc.perform(get("/private/")).andExpect(status().isOk());
		mvc.perform(get("/admin/")).andExpect(status().isOk());
	}

	@Test
	void testAdminAuthority() throws Exception
	{
		User u = new User();
		u.setEmail("a@b.c");
		u.setPassword("...");
		u.setFirstName("a");
		u.setLastName("b");
		u.setAdministrator(true);

		users.save(u);

		mvc.perform(get("/admin/").with(user(u))).andExpect(status().isOk());
	}

	@Test
	void testValidAccountCreationAndLoginErrors() throws Exception
	{
		// Valid signup
		mvc.perform(post("/api/signup").content("""
			{
				"email": "a@b.c",
				"password": "123456",
				"firstName": "a",
				"lastName": "b"
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf()))
			.andExpect(status().isOk());

		// Invalid e-mail address login
		mvc.perform(post("/api/login").content("""
			{
				"username": "invalid@b.c",
				"password": "123456"
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf()))
			.andExpectAll(
				status().isUnauthorized(),
				jsonPath("$.message").value("Adresse e-mail invalide")
			);

		// Invalid password login
		mvc.perform(post("/api/login").content("""
			{
				"username": "a@b.c",
				"password": "invalid"
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf()))
			.andExpectAll(
				status().isUnauthorized(),
				jsonPath("$.message").value("Mot de passe incorrecte")
			);

		// Valid login
		mvc.perform(post("/api/login").content("""
			{
				"username": "a@b.c",
				"password": "123456"
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf()))
			.andExpect(status().isOk());
	}

	@Test
	void testInvalidAccountCreation() throws Exception
	{
		// Invalid email address
		mvc.perform(post("/api/signup").content("""
			{
				"email": "ab.c",
				"password": "123456",
				"firstName": "a",
				"lastName": "b"
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isBadRequest());

		// Password too short
		mvc.perform(post("/api/signup").content("""
			{
				"email": "a@b.c",
				"password": "123",
				"firstName": "a",
				"lastName": "b"
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isBadRequest());

		// Missing first name
		mvc.perform(post("/api/signup").content("""
			{
				"email": "a@b.c",
				"password": "123456",
				"firstName": "",
				"lastName": "b"
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isBadRequest());

		// Missing last name
		mvc.perform(post("/api/signup").content("""
			{
				"email": "a@b.c",
				"password": "123456",
				"firstName": "a",
				"lastName": ""
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isBadRequest());
	}

	@Test
	void testDuplicateAccountCreation() throws Exception
	{
		mvc.perform(post("/api/signup").content("""
			{
				"email": "a@b.c",
				"password": "123456",
				"firstName": "a",
				"lastName": "b"
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isOk());

		mvc.perform(post("/api/signup").content("""
			{
				"email": "a@b.c",
				"password": "7891011",
				"firstName": "b",
				"lastName": "c"
			}
		""").contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isBadRequest());
	}
}
