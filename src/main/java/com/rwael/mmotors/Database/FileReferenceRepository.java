package com.rwael.mmotors.Database;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public interface FileReferenceRepository extends CrudRepository<FileReference, Integer>, FileRefRepositoryEx
{
	Integer countByFile(File file);
}

interface FileRefRepositoryEx
{
	List<FileReference> findByFile(File file);
}

class FileReferenceRepositoryImpl implements FileRefRepositoryEx
{
	private final EntityManager manager;

	public FileReferenceRepositoryImpl(EntityManager manager)
	{
		this.manager = manager;
	}

	@Override
	public List<FileReference> findByFile(File file)
	{
		TypedQuery<FileReference> query = manager.createQuery("SELECT DISTINCT fr FROM FileReference fr WHERE fr.file_id = :id", FileReference.class);
		query.setParameter("id", file.getId());
		return query.getResultList();
	}
}