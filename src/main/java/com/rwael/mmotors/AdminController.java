package com.rwael.mmotors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rwael.mmotors.Database.VehicleRepository;

@Controller
@RequestMapping("/admin")
public class AdminController extends ControllerBase
{
	private final VehicleRepository vehicles;

	public AdminController(VehicleRepository vehicles)
	{
		this.vehicles = vehicles;
	}

	@GetMapping("/")
	public String getIndex(Model model)
	{
		model.addAttribute("vehicles", vehicles.findAll());
		return "admin/index";
	}
}
