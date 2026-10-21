package com.rwael.mmotors.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.UserRepository;

public class ControllerBase
{
	protected boolean isAdmin;
	protected User user;

	private final UserRepository users;

	public ControllerBase(UserRepository users)
	{
		this.users = users;
	}

	@ModelAttribute
	public void isLoggedIn(Model model, Authentication auth)
	{
		if(auth != null)
		{
			isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
			user = users.findByEmail(auth.getName());
		}
		else
		{
			isAdmin = false;
			user = null;
		}

		model.addAttribute("loggedIn", user != null);
		model.addAttribute("isAdmin", isAdmin);
	}
}
