package com.rwael.mmotors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

public class ControllerBase
{
	@ModelAttribute
	public void isLoggedIn(Model model, Authentication auth)
	{
		model.addAttribute("loggedIn", auth != null ? auth.isAuthenticated() : false);
		model.addAttribute("isAdmin", auth != null ? auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) : false);
	}
}
