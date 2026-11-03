package com.rwael.mmotors.Database;

import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications
{
	public static Specification<Order> pending(boolean pending) {
		return (root, query, cb) -> cb.equal(root.get("approvalDate").isNull(), pending);
	}

	public static Specification<Order> approved(boolean approved) {
		return (root, query, cb) -> cb.equal(root.get("approved"), approved);
	}

	public static Specification<Order> forRent(boolean renting) {
		return (root, query, cb) -> cb.equal(root.get("vehicle").get("forRenting"), renting);
	}
}
