package com.asidipta.file.webservices.file_upload.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService implements StorageService {
	
	private final Path root;
	
	@Autowired
	public FileStorageService(StorageProperties props) {
		this.root = Paths.get(props.getLocation());
	}
	
	@Override
	public void store(MultipartFile file) {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		try {
			if(file.isEmpty()) {
				throw new StorageException("Trying to store empty file " + fileName);
			}
			if(fileName.contains("..")) {
				throw new StorageException("Cannot store file with relative path" +fileName);
			}
			try (InputStream inputStream = file.getInputStream()){
				Files.copy(inputStream, this.root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
			}
		}catch (IOException e) {
			throw new StorageException("Failed to store file" + fileName, e);
		}
	}
	
	@Override
	public Stream<Path> loadAll(){
		try {
			return Files.walk(this.root, 1)
					.filter(path -> !path.equals(this.root))
					.map(this.root::relativize);
		}catch(IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}
	
	@Override
	public Path loadFile(String fileName) {
		return root.resolve(fileName);
	}
	
	@Override
	public Resource loadAsResource(String fileName) {
		try {
			Path file = loadFile(fileName);
			Resource resource = new UrlResource(file.toUri());
			if(resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException("Could not read file "+fileName);
			}
		}catch(MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file "+ fileName, e);
		}
	}
	
	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(root.toFile());
	}
	
	@Override
	public void init() {
		try {
			Files.createDirectories(root);
		}catch(IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
	
}
