package com.rwael.mmotors;

import org.springframework.web.bind.annotation.RestController;

import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

record SignupData(
	String email,
	String password,
	String firstName,
	String lastName
) {}

record SignupResult(boolean success, String message) {}

@RestController
@RequestMapping("/api")
public class ApiController
{
	private final ObjectMapper mapper = new ObjectMapper();
	private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

	private final PasswordEncoder encoder;
	private final UserRepository users;

	public ApiController(PasswordEncoder enc, UserRepository userRepo)
	{
		encoder = enc;
		users = userRepo;
	}

	@PostMapping("/signup")
	public String postSignup(@RequestBody SignupData entity)
	{
		System.out.println("SIGNUP: " + entity);

		User other = users.findByEmail(entity.email());

		if(other == null)
		{
			if(entity.password().length() < 6)
				return mapper.writeValueAsString(new SignupResult(false, "Le mot de passe doit contenir au moins 6 caractères"));

			User u = new User();

			u.setEmail(entity.email());
			u.setPassword(encoder.encode(entity.password()));
			u.setFirstName(entity.firstName());
			u.setLastName(entity.lastName());

			users.save(u);

			return mapper.writeValueAsString(new SignupResult(true, ""));
		}
		else
			return mapper.writeValueAsString(new SignupResult(false, "Adresse e-mail déjà prise"));
	}

	@GetMapping("/logout")
	public String getLogout(Authentication auth, HttpServletRequest request, HttpServletResponse response)
	{
		logoutHandler.logout(request, response, auth);
		return "";
	}
}
