package com.rwael.mmotors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rwael.mmotors.Database.FileReference;
import com.rwael.mmotors.Database.FileService;
import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;

import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;

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

record ThumbData(
	Integer vehicleId,
	Integer refId
) {}

@RestController
@RequestMapping("/api/admin")
public class ApiAdminController
{
	private final FileService files;
	private final VehicleRepository vehicles;

	public ApiAdminController(FileService files, VehicleRepository vehicles)
	{
		this.files = files;
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

	@PostMapping("/delete-vehicle")
	public ApiResult postDeleteVehicle(@RequestBody VehicleData entity, HttpServletResponse response)
	{
		Vehicle v = vehicles.findById(entity.id()).orElse(null);

		if(v == null)
			return ApiResult.Get(false, "Aucun véhicule avec cet identifiant", response);

		vehicles.delete(v);
		return ApiResult.Get(true, "", response);
	}

	@PostMapping("/add-vehicle-thumbs")
	public ApiResult postAddVehicleThumbs(@RequestParam MultipartFile[] files, @RequestParam Integer id, HttpServletResponse response)
	{
		FileNameMap map = URLConnection.getFileNameMap();

		Vehicle v = vehicles.findById(id).orElse(null);

		if(v == null)
			return ApiResult.Get(false, "Aucun véhicule avec cet identifiant", response);

		for(MultipartFile file : files)
		{
			String type = map.getContentTypeFor(file.getOriginalFilename());

			if(!type.equals("image/png") && !type.equals("image/jpeg"))
				return ApiResult.Get(false, "Les fichiers doivent être au format PNG, JPG ou JPEG", response);
		}

		for(MultipartFile file : files)
		{
			try
			{
				String name = file.getOriginalFilename();

				v.getThumbs().add(this.files.getReference(file.getBytes(), name.substring(name.lastIndexOf('.') + 1)));

				vehicles.save(v);
			}
			catch(IOException e)
			{
				return ApiResult.Get(false, "Erreur lors de la sauvegarde des fichiers", response);
			}
		}

		return ApiResult.Get(true, "", response);
	}

	@PostMapping("/delete-vehicle-thumbs")
	public ApiResult postDeleteVehicleThumbs(@RequestBody ThumbData thumb, HttpServletResponse response)
	{
		Vehicle v = vehicles.findById(thumb.vehicleId()).orElse(null);

		if(v == null)
			return ApiResult.Get(false, "Aucun véhicule avec cet identifiant", response);

		boolean success = false;

		for(FileReference ref : v.getThumbs())
		{
			if(ref.getId() == thumb.refId())
			{
				success = true;
				v.getThumbs().remove(ref);
				vehicles.save(v);
				files.dropReference(ref);
				break;
			}
		}

		if(success)
			return ApiResult.Get(true, "", response);
		else
			return ApiResult.Get(false, "Cette image n'existe pas", response);
	}
}
