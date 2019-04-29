package tmdad.chat.bbdd;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import tmdad.chat.model.Chatroom;

public interface ChatroomRepository extends CrudRepository<Chatroom, Integer> {

	@Query("SELECT c FROM Chatroom c WHERE c.name = :name")
    public List<Chatroom> findByName(@Param("name") String name);

	@Query("SELECT c FROM Chatroom c WHERE c.name = :name AND c.multipleusers = :multiple")
    public List<Chatroom> findByNameMul(@Param("name") String name, @Param("multiple") boolean multiple);
	

	@Query("SELECT c.name FROM Chatroom c")
    public List<String> findNames();

}