package com.rwael.mmotors.Database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.util.DigestUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public interface FileRepository extends CrudRepository<File, Integer>, FileRepositoryEx
{
	File findByHash(String hash);
	boolean existsByHash(String hash);

	File findByHashAndExtension(String hash, String extension);
	boolean existsByHashAndExtension(String hash, String extension);
}

interface FileRepositoryEx
{
	File findByContents(byte[] contents);
}

class FileRepositoryImpl implements FileRepositoryEx
{
	private final EntityManager manager;

	public FileRepositoryImpl(EntityManager manager)
	{
		this.manager = manager;
	}

	@Override
	public File findByContents(byte[] contents)
	{
		TypedQuery<File> query = manager.createQuery("SELECT DISTINCT f FROM File f WHERE f.hash = :hash", File.class);
		query.setParameter("hash", DigestUtils.md5DigestAsHex(contents));
		return query.getSingleResultOrNull();
	}
}