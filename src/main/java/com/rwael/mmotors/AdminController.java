package com.rwael.mmotors;

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
		model.addAttribute("vehicles", vehicles.findAll());
		return "admin/index";
	}

	@GetMapping("/add-vehicle")
	public String getAddVehicle()
	{
		return "admin/add-vehicle";
	}

	@GetMapping("/edit-vehicle/{id}")
	public String getEditVehicle(Model model, @PathVariable Integer id)
	{
		model.addAttribute("vehicleId", id);
		return "admin/edit-vehicle";
	}
}
