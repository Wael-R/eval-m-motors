package com.rwael.mmotors.Database;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
			f = file;

		FileReference ref = new FileReference();
		ref.setFile(f);

		return ref;
	}

	public FileReference getReference(byte[] contents, String ext)
	{
		File f = new File();
		f.setContent(contents);
		f.setExtension(ext);

		return getReference(f);
	}

	public FileReference getReference(MultipartFile file) throws IOException
	{
		String name = file.getOriginalFilename();

		return getReference(file.getBytes(), name.substring(name.lastIndexOf('.') + 1));
	}

	public void dropReference(FileReference ref)
	{
		File f = ref.getFile();
		refs.delete(ref);

		if(refs.countByFile(f) <= 0)
			files.delete(f);
	}
}
