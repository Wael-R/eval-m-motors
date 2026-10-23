package com.rwael.mmotors.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rwael.mmotors.Database.UserRepository;
import com.rwael.mmotors.Database.VehicleRepository;

@Controller
@RequestMapping("/admin")
public class AdminController extends ControllerBase
{
	private final VehicleRepository vehicles;

	public AdminController(VehicleRepository vehicles, UserRepository user)
	{
		super(user);
		this.vehicles = vehicles;
	}

	@GetMapping("/")
	public String getIndex(Model model)
	{
		return "admin/index";
	}

	@GetMapping("/vehicles/")
	public String getVehiclesIndex(Model model)
	{
		return "admin/vehicles";
	}

	@GetMapping("/vehicles/add")
	public String getAddVehicle(Model model)
	{
		model.addAttribute("edit", false);
		return "admin/edit-vehicle";
	}

	@GetMapping("/vehicles/edit/{id}")
	public String getEditVehicle(Model model, @PathVariable Integer id)
	{
		model.addAttribute("edit", true);
		model.addAttribute("vehicleId", id);
		return "admin/edit-vehicle";
	}
}
