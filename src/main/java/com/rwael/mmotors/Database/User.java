package com.rwael.mmotors.Database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString 
public class User {
	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	private String email;
	private String password;

	private String firstName;
	private String lastName;
}
