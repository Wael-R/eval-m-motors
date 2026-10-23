package com.rwael.mmotors.Database;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public interface VehicleRepository extends CrudRepository<Vehicle, Integer>, PagingAndSortingRepository<Vehicle, Integer>, VehicleRepositoryEx
{
	Page<Vehicle> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(String brand, String model, Pageable pageable);
}

interface VehicleRepositoryEx
{
	List<Vehicle> findByBrand(String brand);
	List<Vehicle> findByModel(String model);
	Vehicle findByBrandAndModel(String brand, String model);
}

class VehicleRepositoryImpl implements VehicleRepositoryEx
{
	private final EntityManager manager;

	public VehicleRepositoryImpl(EntityManager manager)
	{
		this.manager = manager;
	}

	@Override
	public List<Vehicle> findByBrand(String brand)
	{
		TypedQuery<Vehicle> query = manager.createQuery("SELECT DISTINCT v FROM Vehicle v WHERE v.brand = :brand", Vehicle.class);
		query.setParameter("brand", brand);
		return query.getResultList();
	}

	@Override
	public List<Vehicle> findByModel(String model)
	{
		TypedQuery<Vehicle> query = manager.createQuery("SELECT DISTINCT v FROM Vehicle v WHERE v.model = :model", Vehicle.class);
		query.setParameter("model", model);
		return query.getResultList();
	}

	@Override
	public Vehicle findByBrandAndModel(String brand, String model)
	{
		TypedQuery<Vehicle> query = manager.createQuery("SELECT DISTINCT v FROM Vehicle v WHERE v.brand = :brand AND v.model = :model", Vehicle.class);
		query.setParameter("brand", brand);
		query.setParameter("model", model);
		return query.getSingleResultOrNull();
	}
}