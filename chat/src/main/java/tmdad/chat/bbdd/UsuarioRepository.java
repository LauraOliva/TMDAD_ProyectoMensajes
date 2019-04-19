package tmdad.chat.bbdd;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tmdad.chat.model.Usuario;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, String> {
		
	@Query("SELECT u.username FROM Usuario u WHERE u.activeroom = :activeroom")
    public List<String> findByChat(@Param("activeroom") String activeroom); 

}