package tmdad.chat.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tmdad.chat.bbdd.DBAdministrator;
import tmdad.chat.model.DBFile;
import tmdad.chat.model.UploadFileResponse;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private DBFileStorageService DBFileStorageService;
    

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
    	
        DBFile dbFile = DBFileStorageService.storeFile(file);
        System.err.println("Upload file");

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(dbFile.getId())
                .toUriString();

        return new UploadFileResponse(dbFile.getFileName(), fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @GetMapping("/downloadFile/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        System.err.println("Download file");
    	
    	/* TODO comprobar que el usuario tenga permisos */
        // Load file from database
        DBFile dbFile = DBFileStorageService.getFile(fileId);
    	return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dbFile.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
                .body(new ByteArrayResource(dbFile.getData()));
    
        
    }

}