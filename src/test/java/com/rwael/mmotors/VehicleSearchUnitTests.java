package com.rwael.mmotors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.rwael.mmotors.Database.Vehicle;
import com.rwael.mmotors.Database.VehicleRepository;
import com.rwael.mmotors.Database.VehicleSearchService;
import com.rwael.mmotors.Database.VehicleSearchService.FilterMode;
import com.rwael.mmotors.Database.VehicleSearchService.SortMode;

@ExtendWith(MockitoExtension.class)
class VehicleSearchUnitTests
{
	@Mock
	VehicleRepository vehicles;

	VehicleSearchService search;

	Vehicle fordFocus;
	Vehicle fordFiesta;
	Vehicle toyotaCorolla;
	Vehicle volkswagenGolf;
	Vehicle unavailableVehicle;

	private Vehicle createVehicle(Integer id, String brand, String model, Integer price, Integer availability, Boolean renting)
	{
		Vehicle v = new Vehicle();
		v.setId(id);
		v.setBrand(brand);
		v.setModel(model);
		v.setPrice(price);
		v.setAvailability(availability);
		v.setDescription("Test vehicle");
		v.setForRenting(renting);

		return v;
	}

	@BeforeEach
	void setup()
	{
		search = new VehicleSearchService(vehicles);

		fordFocus = createVehicle(1, "Ford", "Focus", 15000, 2, false);
		fordFiesta = createVehicle(2, "Ford", "Fiesta", 10000, 3, false);
		toyotaCorolla = createVehicle(3, "Toyota", "Corolla", 18000, 1, false);
		volkswagenGolf = createVehicle(4, "Volkswagen", "Golf", 20000, 4, true);
		unavailableVehicle = createVehicle(5, "Ford", "Mustang", 30000, 0, false);

		when(vehicles.findByAvailabilityGreaterThan(0)).thenReturn(List.of(fordFocus, fordFiesta, toyotaCorolla, volkswagenGolf));
	}

	@Test
	void testExactBrandSearch()
	{
		Page<Vehicle> result = search.search("Ford", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Any);

		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(fordFocus));
		assertTrue(result.getContent().contains(fordFiesta));
	}

	@Test
	void testExactModelSearch()
	{
		Page<Vehicle> result = search.search("Focus", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Any);

		assertEquals(1, result.getTotalElements());
		assertEquals(fordFocus, result.getContent().get(0));
	}

	@Test
	void testCaseInsensitiveSearch()
	{
		Page<Vehicle> result = search.search("fOrD", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Any);

		assertEquals(2, result.getTotalElements());
	}

	@Test
	void testSearchNormalizesWhitespace()
	{
		Page<Vehicle> result = search.search("  FORD   FOCUS  ", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Any);

		assertEquals(2, result.getTotalElements());
		assertEquals(fordFocus, result.getContent().get(0));
	}

	@Test
	void testFuzzySearch()
	{
		Page<Vehicle> result = search.search("Focs", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Any);

		assertTrue(result.getContent().contains(fordFocus));
	}

	@Test
	void testSearchMatchesBrandAndModel()
	{
		Page<Vehicle> result = search.search("Ford Focus", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Any);

		assertEquals(2, result.getTotalElements());
		assertEquals(fordFocus, result.getContent().get(0));
	}

	@Test
	void testSearchReturnsMostRelevantFirst()
	{
		Page<Vehicle> result = search.search("Ford Focus", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Any);

		assertEquals(fordFocus, result.getContent().get(0));
	}

	@Test
	void testSortByPriceAscending()
	{
		Page<Vehicle> result = search.search("", PageRequest.of(0, 10), SortMode.PriceAsc, FilterMode.Any);

		assertEquals(List.of(fordFiesta, fordFocus, toyotaCorolla, volkswagenGolf), result.getContent());
	}

	@Test
	void testSortByPriceDescending()
	{
		Page<Vehicle> result = search.search("", PageRequest.of(0, 10), SortMode.PriceDesc, FilterMode.Any);

		assertEquals(List.of(volkswagenGolf, toyotaCorolla, fordFocus, fordFiesta), result.getContent());
	}

	@Test
	void testFilterBySale()
	{
		Page<Vehicle> result = search.search("", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Sale);

		assertEquals(List.of(fordFocus, fordFiesta, toyotaCorolla), result.getContent());
	}

	@Test
	void testFilterByRent()
	{
		Page<Vehicle> result = search.search("", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Rent);

		assertEquals(List.of(volkswagenGolf), result.getContent());
	}

	@Test
	void testFirstPage()
	{
		Page<Vehicle> result = search.search("", PageRequest.of(0, 2), SortMode.PriceAsc, FilterMode.Any);

		assertEquals(4, result.getTotalElements());
		assertEquals(2, result.getTotalPages());
		assertEquals(2, result.getContent().size());
		assertEquals(fordFiesta, result.getContent().get(0));
		assertEquals(fordFocus, result.getContent().get(1));
		assertTrue(result.hasNext());
		assertTrue(!result.hasPrevious());
	}

	@Test
	void testSecondPage()
	{
		Page<Vehicle> result = search.search("", PageRequest.of(1, 2), SortMode.PriceAsc, FilterMode.Any);

		assertEquals(4, result.getTotalElements());
		assertEquals(2, result.getContent().size());
		assertEquals(toyotaCorolla, result.getContent().get(0));
		assertEquals(volkswagenGolf, result.getContent().get(1));
		assertTrue(result.hasPrevious());
		assertTrue(!result.hasNext());
	}

	@Test
	void testPageBeyondAvailableResults()
	{
		Page<Vehicle> result = search.search("", PageRequest.of(5, 2), SortMode.PriceAsc, FilterMode.Any);

		assertTrue(result.getContent().isEmpty());
		assertEquals(4, result.getTotalElements());
		assertEquals(2, result.getTotalPages());
	}

	@Test
	void testPageWithNoMatches()
	{
		Page<Vehicle> result = search.search("zzzzzzzz", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Any);

		assertTrue(result.getContent().isEmpty());
		assertEquals(0, result.getTotalElements());
		assertEquals(0, result.getTotalPages());
	}

	@Test
	void testNullQuery()
	{
		Page<Vehicle> result = search.search(null, PageRequest.of(0, 10), SortMode.PriceAsc, FilterMode.Any);

		assertEquals(4, result.getTotalElements());
	}

	@Test
	void testBlankQuery()
	{
		Page<Vehicle> result = search.search("   ", PageRequest.of(0, 10), SortMode.PriceAsc, FilterMode.Any);

		assertEquals(4, result.getTotalElements());
	}

	@Test
	void testRepositoryFiltersAvailability()
	{
		search.search("Ford", PageRequest.of(0, 10), SortMode.Relevance, FilterMode.Any);

		verify(vehicles).findByAvailabilityGreaterThan(0);
	}
}