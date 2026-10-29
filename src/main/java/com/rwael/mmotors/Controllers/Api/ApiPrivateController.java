package com.rwael.mmotors.Controllers.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rwael.mmotors.ApiResult;
import com.rwael.mmotors.Database.FileReference;
import com.rwael.mmotors.Database.FileService;
import com.rwael.mmotors.Database.Order;
import com.rwael.mmotors.Database.OrderRepository;
import com.rwael.mmotors.Database.User;
import com.rwael.mmotors.Database.UserRepository;
import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/private")
public class ApiPrivateController
{
	private final OrderRepository orders;
	private final VehicleRepository vehicles;
	private final UserRepository users;
	private final FileService files;

	public ApiPrivateController(OrderRepository orders, VehicleRepository vehicles, UserRepository users, FileService files)
	{
		this.orders = orders;
		this.vehicles = vehicles;
		this.users = users;
		this.files = files;
	}

	@PostMapping("/order")
	public ApiResult postOrder(@RequestParam(name = "files", required = false) MultipartFile[] upload, @RequestParam Integer id, HttpServletResponse response, Authentication auth)
	{
		if(upload == null || upload.length <= 0)
			return ApiResult.Get(false, "Le dossier est obligatoire", response);

		if(upload.length > 1)
			return ApiResult.Get(false, "Le dossier doit être en un seul fichier", response);

		Vehicle v = vehicles.findById(id).orElse(null);

		if(v == null)
			return ApiResult.Get(false, "Ce véhicule n'existe pas", response);

		if(v.getAvailability() <= 0)
			return ApiResult.Get(false, "Ce véhicule n'est plus disponible", response);

		try
		{
			User u = users.findByEmail(auth.getName());
			Order o = new Order();

			FileReference ref = files.getReference(upload[0]);
			ref.getFile().setOwner(u);
			ref.getFile().setSecure(true);

			o.setUser(u);
			o.setVehicle(v);
			o.setForm(ref);
			o.setDate(Timestamp.valueOf(LocalDateTime.now()));
			o.setApprovalDate(null);
			o.setApproved(false);

			orders.save(o);
		}
		catch(IOException e)
		{
			return ApiResult.Get(false, "Erreur lors de la sauvegarde du fichier", response);
		}

		return ApiResult.Get(true, "", response);
	}
}
