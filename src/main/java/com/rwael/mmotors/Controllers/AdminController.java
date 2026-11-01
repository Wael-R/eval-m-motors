package com.rwael.mmotors.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.rwael.mmotors.Database.Order;
import com.rwael.mmotors.Database.OrderRepository;
import com.rwael.mmotors.Database.UserRepository;
import com.rwael.mmotors.Database.VehicleRepository;

@Controller
@RequestMapping("/admin")
public class AdminController extends ControllerBase
{
	private final VehicleRepository vehicles;
	private final OrderRepository orders;

	public AdminController(VehicleRepository vehicles, OrderRepository orders, UserRepository user)
	{
		super(user);
		this.vehicles = vehicles;
		this.orders = orders;
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

	@GetMapping("/orders/")
	public String getOrdersIndex(Model model)
	{
		return "admin/orders";
	}

	@GetMapping("/orders/{id}")
	public String getOrderDetails(@PathVariable Integer id, Model model)
	{
		Order o = orders.findById(id).orElse(null);

		if(o == null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ce dossier n'existe pas");

		model.addAttribute(o);
		return "admin/approve-order";
	}
}
