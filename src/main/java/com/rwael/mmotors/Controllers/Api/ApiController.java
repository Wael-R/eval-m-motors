package com.rwael.mmotors.Controllers.Api;

import org.springframework.web.bind.annotation.RestController;

import com.rwael.mmotors.ApiResult;
import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.TransactionSystemException;
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

@RestController
@RequestMapping("/api")
public class ApiController
{
	private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
	private final Logger logger = LoggerFactory.getLogger(ApiController.class);

	private final PasswordEncoder encoder;
	private final UserRepository users;

	public ApiController(PasswordEncoder encoder, UserRepository users)
	{
		this.encoder = encoder;
		this.users = users;
	}

	@PostMapping("/signup")
	public ApiResult postSignup(@RequestBody SignupData entity, HttpServletResponse response)
	{
		User other = users.findByEmail(entity.email());

		if(other == null)
		{
			if(entity.password().length() < 6)
			{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ApiResult(false, "Le mot de passe doit contenir au moins 6 caractères");
			}

			User u = new User();

			u.setEmail(entity.email());
			u.setPassword(encoder.encode(entity.password()));
			u.setFirstName(entity.firstName());
			u.setLastName(entity.lastName());

			try {
				users.save(u);
			} catch(TransactionSystemException e) {
				String message = "Erreur lors de l'inscription";

				if(e.getRootCause() instanceof ConstraintViolationException ex)
					message = ex.getConstraintViolations().iterator().next().getMessage();

				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ApiResult(false, message);
			}

			logger.info("New account signed up: account id " + u.getId());

			return new ApiResult(true, "");
		}
		else
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ApiResult(false, "Adresse e-mail déjà prise");
		}
	}

	@GetMapping("/logout")
	public String getLogout(Authentication auth, HttpServletRequest request, HttpServletResponse response)
	{
		logoutHandler.logout(request, response, auth);
		return "";
	}
}
