package com.rwael.mmotors.Database;

import org.springframework.stereotype.Service;

@Service
public class FileService {
	private final FileRepository files;
	private final FileReferenceRepository refs;

	public FileService(FileRepository files, FileReferenceRepository refs)
	{
		this.files = files;
		this.refs = refs;
	}

	public FileReference getReference(File file)
	{
		File f = files.findByHash(file.getHash());
		if(f == null)
			files.save(file);

		FileReference ref = new FileReference();
		ref.setFile(file);

		refs.save(ref);

		return ref;
	}

	public FileReference getReference(byte[] contents, String ext)
	{
		File f = new File();
		f.setContent(contents);
		f.setExtension(ext);

		return getReference(f);
	}

	public FileReference getReference(Integer id) // TODO? remove this (testing)
	{
		return refs.findById(id).get();
	}

	public void dropReference(FileReference ref)
	{
		File f = ref.getFile();
		refs.delete(ref);

		if(refs.countByFile(f) <= 0)
			files.delete(f);
	}
}
