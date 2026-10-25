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
}
