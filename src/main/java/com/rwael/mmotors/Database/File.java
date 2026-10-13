package com.rwael.mmotors.Database;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.DigestUtils;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(
	name = "files",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"hash"}),
	})
public class File
{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	private byte[] content;

	@NotBlank
	@Setter(AccessLevel.NONE)
	private String hash;

	@NotBlank
	private String extension;

	private boolean secure;

	@Basic(fetch = FetchType.LAZY)
	private User owner;

	public void setContent(byte[] content)
	{
		this.content = content;
		hash = DigestUtils.md5DigestAsHex(content);
	}

	public Resource getAsResource()
	{
		return new ByteArrayResource(getContent());
	}
}
