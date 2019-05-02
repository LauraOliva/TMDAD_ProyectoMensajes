package com.tmdad.ficheros;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tmdad.ficheros.model.DBFile;
import com.tmdad.ficheros.model.DBFileRepository;

@Service
public class DBFileStorageService {

    @Autowired
    private DBFileRepository dbFileRepository;

    public DBFile storeFile(MultipartFile file) {
        // Normalize file name
    	/* TODO avisar al usuario en caso de error */
    	/* TODO implementar excepciones para los casos comentados y para 
    	 * el caso en el que el fichero exceda 1MB*/
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                //throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            	System.err.println("Nombre de fichero inválido");
            }

            DBFile dbFile = new DBFile(fileName, file.getContentType(), file.getBytes());

            return dbFileRepository.save(dbFile);
        } catch (IOException ex) {
            //throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        	System.err.println("No se ha podido almacenar el fichero " + fileName);
        }
        return null;
    }

    public DBFile getFile(String fileId) {
    	DBFile file = dbFileRepository.findById(fileId).orElse(null);
    	return file;
    }
}
