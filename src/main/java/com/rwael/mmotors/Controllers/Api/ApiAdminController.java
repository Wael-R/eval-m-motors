package com.rwael.mmotors.Controllers.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rwael.mmotors.ApiResult;
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

	ApiResult saveVehicle(Vehicle v, HttpServletResponse response)
	{
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

		return ApiResult.Get(true, "", v, response);
	}

	String addVehicleThumbs(Vehicle v, MultipartFile[] upload)
	{
		FileNameMap map = URLConnection.getFileNameMap();

		for(MultipartFile file : upload)
		{
			String type = map.getContentTypeFor(file.getOriginalFilename());

			if(!type.equals("image/png") && !type.equals("image/jpeg"))
				return "Les fichiers doivent être au format PNG, JPG ou JPEG";
		}

		for(MultipartFile file : upload)
		{
			try
			{
				String name = file.getOriginalFilename();

				v.getThumbs().add(files.getReference(file.getBytes(), name.substring(name.lastIndexOf('.') + 1)));
			}
			catch(IOException e)
			{
				return "Erreur lors de la sauvegarde des fichiers";
			}
		}

		return null;
	}

	@PostMapping("/add-vehicle")
	public ApiResult postAddVehicle(Vehicle entity, @RequestParam(name = "files", required = false) MultipartFile[] upload, HttpServletResponse response)
	{
		Vehicle other = vehicles.findByBrandAndModel(entity.getBrand(), entity.getModel());

		if(other == null)
		{
			if(upload != null && upload.length > 0)
			{
				String res = addVehicleThumbs(entity, upload);

				if(res != null)
					return ApiResult.Get(false, res, response);
			}

			return saveVehicle(entity, response);
		}
		else
			return ApiResult.Get(false, "Un véhicule existe déjà avec la même marque et modèle", response);
	}

	@PostMapping("/edit-vehicle")
	public ApiResult postEditVehicle(Vehicle entity, @RequestParam(name = "files", required = false) MultipartFile[] upload, HttpServletResponse response)
	{
		Vehicle v = vehicles.findById(entity.getId()).orElse(null);

		if(v == null)
			return ApiResult.Get(false, "Aucun véhicule avec cet identifiant", response);

		if(entity.getModel() != null) v.setModel(entity.getModel());
		if(entity.getBrand() != null) v.setBrand(entity.getBrand());
		if(entity.getDescription() != null) v.setDescription(entity.getDescription());
		if(entity.getAvailability() != null) v.setAvailability(entity.getAvailability());

		if(upload != null && upload.length > 0)
		{
			String res = addVehicleThumbs(v, upload);

			if(res != null)
				return ApiResult.Get(false, res, response);
		}

		return saveVehicle(v, response);
	}

	@PostMapping("/delete-vehicle")
	public ApiResult postDeleteVehicle(@RequestBody Vehicle entity, HttpServletResponse response)
	{
		Vehicle v = vehicles.findById(entity.getId()).orElse(null);

		if(v == null)
			return ApiResult.Get(false, "Aucun véhicule avec cet identifiant", response);

		vehicles.delete(v);
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
			if(ref.getId().equals(thumb.refId()))
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