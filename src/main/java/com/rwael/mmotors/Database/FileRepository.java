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
	File findByFilename(String filename);
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
	public File findByFilename(String filename)
	{
		Integer dot = filename.lastIndexOf('.');

		String hash = filename.substring(0, dot);
		String ext = filename.substring(dot + 1);

		TypedQuery<File> query = manager.createQuery("SELECT DISTINCT f FROM File f WHERE f.hash = :hash AND f.extension = :ext", File.class);
		query.setParameter("hash", hash);
		query.setParameter("ext", ext);
		return query.getSingleResultOrNull();
	}

	@Override
	public File findByContents(byte[] contents)
	{
		TypedQuery<File> query = manager.createQuery("SELECT DISTINCT f FROM File f WHERE f.hash = :hash", File.class);
		query.setParameter("hash", DigestUtils.md5DigestAsHex(contents));
		return query.getSingleResultOrNull();
	}
}