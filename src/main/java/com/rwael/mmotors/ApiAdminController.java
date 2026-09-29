package com.rwael.mmotors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

record VehicleData(
	String brand,
	String model,
	String description,
	Integer availability
) {}

@RestController
@RequestMapping("/api/admin")
public class ApiAdminController
{
	private final VehicleRepository vehicles;

	public ApiAdminController(VehicleRepository vehicles)
	{
		this.vehicles = vehicles;
	}

	@PostMapping("/add-vehicle")
	public ApiResult postAddVehicle(@RequestBody VehicleData entity, HttpServletResponse response)
	{
		Vehicle other = vehicles.findByBrandAndModel(entity.brand(), entity.model());

		if(other == null)
		{
			Vehicle v = new Vehicle();
			v.setModel(entity.model());
			v.setBrand(entity.brand());
			v.setDescription(entity.description());
			v.setAvailability(entity.availability());

			try {
				vehicles.save(v);
			} catch(TransactionSystemException e) {
				String message = "Erreur lors de l'ajout du véhicule";

				if(e.getRootCause() instanceof ConstraintViolationException ex)
					message = ex.getConstraintViolations().iterator().next().getMessage();

				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return new ApiResult(false, message);
			}

			return new ApiResult(true, "");
		}
		else
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new ApiResult(false, "Un véhicule existe déjà avec la même marque et modèle");
		}
	}
}
