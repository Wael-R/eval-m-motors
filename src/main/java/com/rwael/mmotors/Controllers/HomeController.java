package com.rwael.mmotors.Controllers;

import java.net.FileNameMap;
import java.net.URLConnection;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.rwael.mmotors.Database.File;
import com.rwael.mmotors.Database.FileRepository;
import com.rwael.mmotors.Database.UserRepository;
import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController extends ControllerBase
{
	private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
	private final FileRepository files;
	private final VehicleRepository vehicles;

	public HomeController(FileRepository files, UserRepository users, VehicleRepository vehicles)
	{
		super(users);
		this.files = files;
		this.vehicles = vehicles;
	}

	@GetMapping("/")
	public String getIndex(Model model)
	{
		return "index";
	}

	@GetMapping("/login")
	public String getLogin()
	{
		return "login";
	}

	@GetMapping("/signup")
	public String getSignup()
	{
		return "signup";
	}

	@GetMapping("/logout")
	public String getLogout(Authentication auth, HttpServletRequest request, HttpServletResponse response)
	{
		logoutHandler.logout(request, response, auth);

		return "redirect:/";
	}

	@GetMapping("/private")
	public String getPrivateTest(Model model)
	{
		return "index";
	}

	@GetMapping("/search")
	public String getSearch(Model model)
	{
		return "search";
	}

	@GetMapping("/vehicles/{id}")
	public String getVehicle(@PathVariable Integer id, Model model)
	{
		Vehicle v = vehicles.findById(id).orElse(null);
		if(v == null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ce véhicule n'existe pas");

		model.addAttribute(v);
		return "vehicle";
	}

	@GetMapping("/content/{filename:.+\\..+}")
	@ResponseBody
	public ResponseEntity<Resource> getResource(@PathVariable String filename)
	{
		File file = files.findByFilename(filename);
		if(file == null)
			return ResponseEntity.notFound().build();
		else
		{
			if(file.isSecure() && !isAdmin && (user == null || file.getOwner() == null || file.getOwner().getId() != user.getId()))
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

			FileNameMap map = URLConnection.getFileNameMap();
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, map.getContentTypeFor(filename)).body(file.getAsResource());
		}
	}
}
