package com.rwael.mmotors.Controllers.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	
	@ModelAttribute
	public void fetchUser(Authentication auth, Model model)
	{
		model.addAttribute(users.findByEmail(auth.getName()));
	}

	@PostMapping("/order")
	public ApiResult postOrder(@RequestParam(name = "files", required = false) MultipartFile[] upload, @RequestParam Integer id, @ModelAttribute User user, HttpServletResponse response)
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
			
			if(orders.findPendingByUserAndVehicle(true, user, v).size() > 0)
				return ApiResult.Get(false, "Vous avez déjà un dossier en cours de traitement pour ce véhicule", response);

			Order o = new Order();

			FileReference ref = files.getReference(upload[0], user);
			ref.getFile().setOwner(user);
			ref.getFile().setSecure(true);

			o.setUser(user);
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

	@GetMapping("/orders/list")
	public Page<Order> getOrderList(@RequestParam(defaultValue = "0") Integer sort, @RequestParam(defaultValue = "0") Integer filter, @ModelAttribute User user, HttpServletResponse response, Pageable pageable)
	{
		if(pageable.getPageSize() > 20)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Taille de page limitée à 20 éléments");

		Integer page = pageable.getPageNumber();
		Integer size = pageable.getPageSize();

		Pageable sorted = PageRequest.of(page, size, Sort.by("date").reverse());

		if(sort == 1)
			sorted = PageRequest.of(page, size, Sort.by("date"));

		if(filter == 0)
			return orders.findByPendingAndUser(true, user, sorted);
		else if(filter == 1)
			return orders.findByApprovedAndUser(true, user, sorted);
		else if(filter == 2)
			return orders.findByApprovedAndUser(false, user, sorted);
		else
			return orders.findByUser(user, sorted);
	}
}
