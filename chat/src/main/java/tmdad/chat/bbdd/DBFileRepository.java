package tmdad.chat.bbdd;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tmdad.chat.model.DBFile;

@Repository
public interface DBFileRepository extends CrudRepository<DBFile, String> {

}
