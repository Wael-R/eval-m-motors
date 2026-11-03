package com.rwael.mmotors.Controllers.Api;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.rwael.mmotors.ApiResult;
import com.rwael.mmotors.Database.Order;
import com.rwael.mmotors.Database.OrderRepository;
import com.rwael.mmotors.Database.OrderSpecifications;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/admin/orders")
public class ApiAdminOrdersController
{
	private final OrderRepository orders;

	public ApiAdminOrdersController(OrderRepository orders)
	{
		this.orders = orders;
	}

	@GetMapping("/list")
	public Page<Order> getList(@RequestParam(defaultValue = "0") Integer sort, @RequestParam(defaultValue = "0") Integer filter, @RequestParam(defaultValue = "0") Integer filterType, HttpServletResponse response, Pageable pageable)
	{
		if(pageable.getPageSize() > 20)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Taille de page limitée à 20 éléments");

		Integer page = pageable.getPageNumber();
		Integer size = pageable.getPageSize();

		Pageable sorted = PageRequest.of(page, size, Sort.by("date"));
		
		if(sort == 1)
			sorted = PageRequest.of(page, size, Sort.by("date").reverse());

		Specification<Order> spec = Specification.unrestricted();

		if(filter == 0)
			spec = spec.and(OrderSpecifications.pending(true));
		else if(filter == 1)
			spec = spec.and(OrderSpecifications.approved(true)).and(OrderSpecifications.pending(false));
		else if(filter == 2)
			spec = spec.and(OrderSpecifications.approved(false)).and(OrderSpecifications.pending(false));

		if(filterType == 1)
			spec = spec.and(OrderSpecifications.forRent(false));
		else if(filterType == 2)
			spec = spec.and(OrderSpecifications.forRent(true));

		return orders.findAll(spec, sorted);
	}

	@PostMapping("/approve")
	public ApiResult postApprove(@RequestParam Integer id, @RequestParam boolean approved, HttpServletResponse response)
	{
		Order o = orders.findById(id).orElse(null);

		if(o == null)
			return ApiResult.Get(false, "Ce dossier n'existe pas", response);

		if(o.getApprovalDate() != null)
			return ApiResult.Get(false, "Ce dossier a déjà été validé", response);

		o.setApproved(approved);
		o.setApprovalDate(Timestamp.valueOf(LocalDateTime.now()));

		orders.save(o);
		return ApiResult.Get(true, "", response);
	}
}
