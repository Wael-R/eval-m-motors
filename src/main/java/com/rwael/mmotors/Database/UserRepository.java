package com.rwael.mmotors.Database;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer>
{
	User findByEmail(String email);
}