package com.rwael.mmotors.Database;

import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public interface UserRepository extends CrudRepository<User, Integer>, UserRepositoryEx { }

interface UserRepositoryEx
{
	User findByEmail(String email);
}

class UserRepositoryImpl implements UserRepositoryEx
{
	private final EntityManager mgr;

	public UserRepositoryImpl(EntityManager manager)
	{
		mgr = manager;
	}

	@Override
	public User findByEmail(String email)
	{
		TypedQuery<User> query = mgr.createQuery("SELECT DISTINCT u FROM User u WHERE u.email = :email", User.class);
		query.setParameter("email", email);
		return query.getSingleResultOrNull();
	}
}