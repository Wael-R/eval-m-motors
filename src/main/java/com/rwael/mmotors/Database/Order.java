package com.rwael.mmotors.Database;

import java.sql.Timestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "orders")
public class Order
{
	@Id
	@GeneratedValue
	private Integer id;

	@ManyToOne
	private User user;

	@ManyToOne
	private Vehicle vehicle;

	@OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
	private FileReference form;

	private boolean approved;

	private Timestamp date;
	private Timestamp approvalDate;
}
