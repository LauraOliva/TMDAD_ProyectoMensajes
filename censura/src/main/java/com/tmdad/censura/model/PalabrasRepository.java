package com.tmdad.censura.model;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;



public interface PalabrasRepository extends CrudRepository<PalabraCensurada, Integer>  {
	@Query("SELECT p FROM PalabraCensurada p WHERE p.palabra = :word")
    public List<PalabraCensurada> findByWord(@Param("word") String word);
}
