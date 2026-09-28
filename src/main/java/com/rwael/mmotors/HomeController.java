package com.rwael.mmotors;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController extends ControllerBase
{
	private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

	@GetMapping("/")
	public String getIndex(Model model)
	{
		return "index";
	}

	@GetMapping("/login")
	public String getLogin()
	{
		return "login";
	}

	@GetMapping("/signup")
	public String getSignup()
	{
		return "signup";
	}

	@GetMapping("/logout")
	public String getLogout(Authentication auth, HttpServletRequest request, HttpServletResponse response)
	{
		logoutHandler.logout(request, response, auth);

		return "redirect:/";
	}

	@GetMapping("/private")
	public String getPrivateTest(Model model)
	{
		return "index";
	}

	@GetMapping("/admin")
	public String getAdminTest(Model model)
	{
		return "index";
	}
}
