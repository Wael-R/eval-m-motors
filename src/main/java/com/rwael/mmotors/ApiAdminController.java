package com.rwael.mmotors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

record VehicleData(
	Integer id,
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

				return ApiResult.Get(false, message, response);
			}

			return ApiResult.Get(true, "", response);
		}
		else
			return ApiResult.Get(false, "Un véhicule existe déjà avec la même marque et modèle", response);
	}

	@PostMapping("/edit-vehicle")
	public ApiResult postEditVehicle(@RequestBody VehicleData entity, HttpServletResponse response)
	{
		Vehicle v = vehicles.findById(entity.id()).orElse(null);

		if(v == null)
			return ApiResult.Get(false, "Aucun véhicule avec cet identifiant", response);

		if(entity.model() != null) v.setModel(entity.model());
		if(entity.brand() != null) v.setBrand(entity.brand());
		if(entity.description() != null) v.setDescription(entity.description());
		if(entity.availability() != null) v.setAvailability(entity.availability());

		try {
			vehicles.save(v);
		} catch(TransactionSystemException e) {
			String message = "Erreur lors de la modification du véhicule";

			if(e.getRootCause() instanceof ConstraintViolationException ex)
				message = ex.getConstraintViolations().iterator().next().getMessage();

			return ApiResult.Get(false, message, response);
		} catch(DataIntegrityViolationException e) {
			return ApiResult.Get(false, "Un véhicule existe déjà avec la même marque et modèle", response);
		}

		return ApiResult.Get(true, "", response);
	}
}
