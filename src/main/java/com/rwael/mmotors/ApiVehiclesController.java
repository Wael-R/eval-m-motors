package com.rwael.mmotors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/vehicles")
public class ApiVehiclesController
{
	private final VehicleRepository vehicles;

	public ApiVehiclesController(VehicleRepository vehicles)
	{
		this.vehicles = vehicles;
	}

	@GetMapping("/details/{id}")
	public ApiResult getDetails(@PathVariable Integer id, HttpServletResponse response)
	{
		Vehicle v = vehicles.findById(id).orElse(null);

		if(v != null)
			return ApiResult.Get(true, "", v, response);
		else
			return ApiResult.Get(false, "Aucun véhicule avec cet identifiant", response);
	}
}
