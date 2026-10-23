package com.rwael.mmotors.Controllers.Api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.rwael.mmotors.ApiResult;
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

	@GetMapping("/list")
	public Page<Vehicle> getList(@RequestParam(defaultValue = "") String query, HttpServletResponse response, @PageableDefault(size = 20) Pageable pageable)
	{
		if(pageable.getPageSize() > 20)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Taille de page limitée à 20 éléments");

		Page<Vehicle> page;

		if(query.length() > 0)
			page = vehicles.findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(query, query, pageable);
		else
			page = vehicles.findAll(pageable);

		return page;
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
