package com.rwael.mmotors;

import org.springframework.web.bind.annotation.RestController;

import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.UserRepository;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

class SignupData {
	public String email;
	public String password;
	public String firstName;
	public String lastName;
}

@RestController
@RequestMapping("/api")
public class ApiController
{
	private final UserRepository users;

	public ApiController(UserRepository userRepo)
	{
		users = userRepo;
	}

	@PostMapping("/login")
	public String postLogin(@RequestBody SignupData entity)
	{
		System.out.println(users.findByEmail(entity.email));
		
		return entity.email;
	}

	@PostMapping("/signup")
	public String postSignup(@RequestBody SignupData entity)
	{
		System.out.println(entity);

		User u = new User();

		u.setEmail(entity.email);
		u.setPassword(entity.password);
		u.setFirstName(entity.firstName);
		u.setLastName(entity.lastName);

		users.save(u);
		
		return entity.email;
	}
}
