package com.rwael.mmotors.Database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "vehicles")
public class Vehicle
{
	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@NotBlank(message = "La marque est obligatoire")
	private String brand;

	@NotBlank(message = "Le modèle est obligatoire")
	private String model;

	private String description;

	private boolean isAvailable;
}
