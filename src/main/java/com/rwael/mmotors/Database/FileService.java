package com.rwael.mmotors.Database;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
	private final FileRepository files;

	public FileService(FileRepository files)
	{
		this.files = files;
	}

	public FileReference getReference(File file, User owner)
	{
		File f = files.findByHashAndOwner(file.getHash(), owner);
		if(f == null)
			f = file;

		FileReference ref = new FileReference();
		ref.setFile(f);

		return ref;
	}

	public FileReference getReference(byte[] contents, String ext, User owner)
	{
		File f = new File();
		f.setContent(contents);
		f.setExtension(ext);

		return getReference(f, owner);
	}

	public FileReference getReference(MultipartFile file, User owner) throws IOException
	{
		String name = file.getOriginalFilename();

		return getReference(file.getBytes(), name.substring(name.lastIndexOf('.') + 1), owner);
	}
}
