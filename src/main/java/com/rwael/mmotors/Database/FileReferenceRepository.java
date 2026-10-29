package com.rwael.mmotors.Database;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface FileReferenceRepository extends CrudRepository<FileReference, Integer>
{
	Integer countByFile(File file);
	List<FileReference> findByFile(File file);
}