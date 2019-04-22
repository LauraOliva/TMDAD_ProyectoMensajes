package tmdad.chat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "files")
public class DBFile {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Getter @Setter 
    private String id;

    @Getter @Setter 
    private String fileName;

    @Getter @Setter 
    private String fileType;

    @Lob
    @Getter @Setter
    private byte[] data;

    public DBFile() {}

    public DBFile(String fileName, String fileType, byte[] data) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
    }

}