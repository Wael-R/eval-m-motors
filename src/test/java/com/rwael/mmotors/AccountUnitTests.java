package com.rwael.mmotors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class AccountUnitTests
{
	@Mock 
	UserRepository repo;

	@InjectMocks 
	UserAuthService users;

	@Test
	void testRepositoryFindsUser()
	{
		User user = new User();
		user.setId(0);
		user.setEmail("a@b.c");
		user.setFirstName("a");
		user.setLastName("b");

		when(repo.findByEmail("a@b.c")).thenReturn(user);
		
		UserDetails details = users.loadUserByUsername("a@b.c");

		assertEquals("a@b.c", details.getUsername());
		assertTrue(details.getAuthorities().stream().anyMatch(au -> au.getAuthority() == "ROLE_USER"));
	}

	@Test
	void testRepositoryMissingUser()
	{
		when(repo.findByEmail("a@b.c")).thenReturn(null);

		assertThrows(UsernameNotFoundException.class, () -> users.loadUserByUsername("a@b.c"));
	}
}
