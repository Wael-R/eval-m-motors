package com.rwael.mmotors.Database;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public interface VehicleRepository extends CrudRepository<Vehicle, Integer>, VehicleRepositoryEx { }

interface VehicleRepositoryEx
{
	List<Vehicle> findByBrand(String brand);
	List<Vehicle> findByModel(String model);
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
}