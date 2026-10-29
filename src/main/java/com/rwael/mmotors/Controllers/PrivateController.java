package com.rwael.mmotors.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.rwael.mmotors.Database.UserRepository;
import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

@Controller
@RequestMapping("/private")
public class PrivateController extends ControllerBase
{
	private final VehicleRepository vehicles;

	public PrivateController(UserRepository users, VehicleRepository vehicles)
	{
		super(users);
		this.vehicles = vehicles;
	}

	@GetMapping("/")
	public String getIndex(Model model)
	{
		return "private/index";
	}

	@GetMapping("/order/{id}")
	public String getOrder(@PathVariable Integer id, Model model)
	{
		Vehicle v = vehicles.findById(id).orElse(null);
		if(v == null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ce véhicule n'existe pas");

		if(v.getAvailability() <= 0)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ce véhicule n'est plus disponible");

		model.addAttribute(v);
		return "private/order-vehicle";
	}
}
