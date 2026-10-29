package com.rwael.mmotors.Database;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer>
{
	Integer countByVehicle(Vehicle vehicle);

	List<Order> findByUser(User user);
	List<Order> findByVehicle(Vehicle vehicle);
	List<Order> findByApproved(boolean approved);

	Page<Order> findByUser(User user, Pageable pageable);
	Page<Order> findByVehicle(Vehicle vehicle, Pageable pageable);
	Page<Order> findByApproved(boolean approved, Pageable pageable);
}