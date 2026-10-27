package com.rwael.mmotors.Database;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer>
{
	List<Vehicle> findByBrand(String brand);
	List<Vehicle> findByModel(String model);
	Vehicle findByBrandAndModel(String brand, String model);
	List<Vehicle> findByAvailabilityGreaterThan(Integer availability);
	Page<Vehicle> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model, Pageable pageable);
}