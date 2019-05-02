package com.tmdad.ficheros.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DBFileRepository extends CrudRepository<DBFile, String> {

}
