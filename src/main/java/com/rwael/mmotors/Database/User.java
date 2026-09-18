package com.rwael.mmotors.Database;

import java.util.ArrayList;
import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString 
public class User implements UserDetails
{
	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Email(message = "Adresse e-mail invalide")
	@NotBlank(message = "L'adresse e-mail est obligatoire")
	private String email;
	@NotBlank
	private String password;

	@NotBlank(message = "Le nom est obligatoire")
	private String firstName;
	@NotBlank(message = "Le nom est obligatoire")
	private String lastName;

	private boolean isAdministrator;

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		ArrayList<GrantedAuthority> roles = new ArrayList<>();
		roles.add(new Role("USER"));
		
		if(isAdministrator)
			roles.add(new Role("ADMIN"));

		return roles;
	}
}

class Role implements GrantedAuthority
{
	@Getter @Setter
	private String role;

	public Role(String role)
	{
		this.role = role;
	}

	@Override
	public @Nullable String getAuthority() {
		return role;
	}
}