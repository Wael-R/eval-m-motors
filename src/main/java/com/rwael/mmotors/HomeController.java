package com.rwael.mmotors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController
{
	@ModelAttribute
	public void isLoggedIn(Model model)
	{
		model.addAttribute("loggedIn", false);
	}

	@GetMapping("/index")
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
}
