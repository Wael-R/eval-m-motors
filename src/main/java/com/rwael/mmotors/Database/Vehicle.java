package com.rwael.mmotors.Database;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(
	name = "vehicles",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"brand", "model"})
	})
public class Vehicle
{
	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@NotBlank(message = "La marque est obligatoire")
	private String brand;

	@NotBlank(message = "Le modèle est obligatoire")
	private String model;

	@NotNull
	@Length(max = 5000, message = "La description est limitée à 5000 caractères")
	private String description;

	@NotNull
	@PositiveOrZero(message = "La disponibilité doit être supérieure ou égale à 0")
	private Integer availability;

	@OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	private List<FileReference> thumbs = new ArrayList<>();
}
