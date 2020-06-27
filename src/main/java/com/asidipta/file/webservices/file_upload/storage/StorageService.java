package com.asidipta.file.webservices.file_upload.storage;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.stream.Stream;

@Repository
public interface StorageService {
	void init();

	void store(MultipartFile file);	//given a file store it

	Stream<Path> loadAll();	//load all files in storage

	Path loadFile(String filename);	//load a file by fileName

	Resource loadAsResource(String fileName);

	void deleteAll();	//delete all files in storage

}
