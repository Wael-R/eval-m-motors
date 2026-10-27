package com.rwael.mmotors.Database;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.StdSerializer;

@Entity
@Getter
@Setter
@ToString
@Table(name = "file_refs")
@JsonSerialize(using = FileRefSerializer.class)
public class FileReference
{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private File file;

	public String getPath()
	{
		File f = getFile();
		return "/content/" + f.getHash() + "." + f.getExtension();
	}
}

class FileRefSerializer extends StdSerializer<FileReference>
{
	public FileRefSerializer(Class<Vehicle> t) { super(t); }
	public FileRefSerializer() { this(null); }

	@Override
	public void serialize(FileReference value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException
	{
		gen.writeStartObject();
		gen.writeNumberProperty("id", value.getId());
		gen.writeStringProperty("path", value.getPath());
		gen.writeEndObject();
	}
}