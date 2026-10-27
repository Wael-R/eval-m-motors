package com.rwael.mmotors.Database;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

record VehicleScore(Vehicle vehicle, double score, Integer price) {}

@Service
public class VehicleSearchService
{
	private final VehicleRepository vehicles;
	private final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

	public VehicleSearchService(VehicleRepository vehicles)
	{
		this.vehicles = vehicles;
	}

	public enum SortMode
	{
		Relevance,
		PriceDesc,
		PriceAsc
	}

	public Page<Vehicle> search(String query, Pageable pageable, SortMode mode)
	{
		String search = normalize(query);

		Stream<VehicleScore> stream = vehicles.findByAvailabilityGreaterThan(0).stream()
			.map(vehicle -> new VehicleScore(
				vehicle,
				score(vehicle, search),
				vehicle.getPrice()
			))
			.filter(match -> match.score() >= 0.75);

		switch(mode)
		{
			case Relevance: stream = stream.sorted(Comparator.comparingDouble(VehicleScore::score).reversed()); break;
			case PriceAsc: stream = stream.sorted(Comparator.comparingInt(VehicleScore::price)); break;
			case PriceDesc: stream = stream.sorted(Comparator.comparingInt(VehicleScore::price).reversed()); break;
			default: break;
		}

		List<Vehicle> matches = stream.map(VehicleScore::vehicle).toList();

		Integer start = (int)pageable.getOffset();

		if(start >= matches.size())
			return new PageImpl<>(new ArrayList<>(), pageable, matches.size());

		Integer end = Math.min(start + pageable.getPageSize(), matches.size());

		return new PageImpl<>(matches.subList(start, end), pageable, matches.size());
	}

	private double score(Vehicle vehicle, String query)
	{
		if(query == null || query.length() <= 0)
			return 1.0;

		String brand = normalize(vehicle.getBrand());
		String model = normalize(vehicle.getModel());
		String name = brand + " " + model;

		double score = Math.max(
			Math.max(
				similarity.apply(query, brand),
				similarity.apply(query, model)
			),
			similarity.apply(query, name)
		);

		return score;
	}

	private String normalize(String value)
	{
		if(value == null)
			return "";

		return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
	}
}