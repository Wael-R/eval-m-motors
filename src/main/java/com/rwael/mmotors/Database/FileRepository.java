package com.rwael.mmotors.Database;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.util.DigestUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public interface FileRepository extends CrudRepository<File, Integer>, FileRepositoryEx
{
	File findByHashAndOwner(String hash, User owner);
	boolean existsByHashAndOwner(String hash, User owner);

	File findByHashAndExtensionAndOwner(String hash, String extension, User owner);
	boolean existsByHashAndExtensionAndOwner(String hash, String extension, User owner);
}

interface FileRepositoryEx
{
	List<File> findByFilename(String filename);
	File findByFilenameAndOwner(String filename, User owner);
	File findByContentsAndOwner(byte[] contents, User owner);
}

class FileRepositoryImpl implements FileRepositoryEx
{
	private final EntityManager manager;

	public FileRepositoryImpl(EntityManager manager)
	{
		this.manager = manager;
	}

	@Override
	public List<File> findByFilename(String filename)
	{
		Integer dot = filename.lastIndexOf('.');

		String hash = filename.substring(0, dot);
		String ext = filename.substring(dot + 1);

		TypedQuery<File> query = manager.createQuery("SELECT DISTINCT f FROM File f WHERE f.hash = :hash AND f.extension = :ext", File.class);
		query.setParameter("hash", hash);
		query.setParameter("ext", ext);
		return query.getResultList();
	}

	@Override
	public File findByFilenameAndOwner(String filename, User owner)
	{
		Integer dot = filename.lastIndexOf('.');

		String hash = filename.substring(0, dot);
		String ext = filename.substring(dot + 1);

		TypedQuery<File> query = manager.createQuery("SELECT DISTINCT f FROM File f WHERE f.hash = :hash AND f.extension = :ext AND owner = :owner", File.class);
		query.setParameter("hash", hash);
		query.setParameter("ext", ext);
		query.setParameter("owner", owner);
		return query.getSingleResultOrNull();
	}

	@Override
	public File findByContentsAndOwner(byte[] contents, User owner)
	{
		TypedQuery<File> query = manager.createQuery("SELECT DISTINCT f FROM File f WHERE f.hash = :hash AND f.owner = :owner", File.class);
		query.setParameter("hash", DigestUtils.md5DigestAsHex(contents));
		query.setParameter("owner", owner);
		return query.getSingleResultOrNull();
	}
}