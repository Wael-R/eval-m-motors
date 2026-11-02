package com.rwael.mmotors.Database;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer>
{
	Integer countByVehicle(Vehicle vehicle);

	List<Order> findByUser(User user);
	List<Order> findByVehicle(Vehicle vehicle);

	Page<Order> findByUser(User user, Pageable pageable);
	Page<Order> findByVehicle(Vehicle vehicle, Pageable pageable);

	@Query("SELECT o FROM Order o WHERE ((o.approvalDate IS NULL AND :pending = true) OR (o.approvalDate IS NOT NULL AND :pending = false)) AND o.user = :user AND o.vehicle = :vehicle")
	List<Order> findPendingByUserAndVehicle(boolean pending, User user, Vehicle vehicle);

	@Query("SELECT o FROM Order o WHERE o.approvalDate IS NOT NULL AND o.approved = :approved")
	List<Order> findByApproved(boolean approved);
	@Query("SELECT o FROM Order o WHERE o.approvalDate IS NOT NULL AND o.approved = :approved")
	Page<Order> findByApproved(boolean approved, Pageable pageable);

	@Query("SELECT o FROM Order o WHERE (o.approvalDate IS NULL AND :pending = true) OR (o.approvalDate IS NOT NULL AND :pending = false)")
	List<Order> findByPending(boolean pending);
	@Query("SELECT o FROM Order o WHERE (o.approvalDate IS NULL AND :pending = true) OR (o.approvalDate IS NOT NULL AND :pending = false)")
	Page<Order> findByPending(boolean pending, Pageable pageable);

	@Query("SELECT o FROM Order o WHERE o.approvalDate IS NOT NULL AND o.approved = :approved AND o.user = :user")
	Page<Order> findByApprovedAndUser(boolean approved, User user, Pageable pageable);
	@Query("SELECT o FROM Order o WHERE ((o.approvalDate IS NULL AND :pending = true) OR (o.approvalDate IS NOT NULL AND :pending = false)) AND o.user = :user")
	Page<Order> findByPendingAndUser(boolean pending, User user, Pageable pageable);
}