package com.rwael.mmotors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	@GetMapping("/index")
	public String getIndex(Model model) {
		model.addAttribute("test", "hello world");
		return "index";
	}
}
