package tmdad.chat.bbdd;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import tmdad.chat.model.Mensaje;

public interface MensajeRepository extends CrudRepository<Mensaje, Integer> {

	@Query("SELECT m FROM Mensaje m WHERE m.type = :type "
			+ "AND m.dst = :dst ORDER BY m.timestamp")
    public List<Mensaje> findMsg(@Param("type") String type, @Param("dst") String dst);

	@Query("SELECT m FROM Mensaje m, Chatroom c WHERE m.type = :type  AND m.dst = :id "
			+ "AND m.timestamp >= :timestamp AND c.multipleusers = :multiple "
			+ "AND m.dst = c.name ORDER BY m.timestamp")
    public List<Mensaje> findMsgChat(@Param("type") String type, @Param("id") String id, 
    		@Param("timestamp") long timestamp, @Param("multiple") boolean multiple);
	
	@Query("SELECT m.timestamp FROM Mensaje m WHERE m.type = :type "
			+ "AND m.dst = :dst AND m.msg = :msg ORDER BY m.timestamp DESC")
    public List<Long> findMsgDate(@Param("type") String type, @Param("dst") String dst, @Param("msg") String msg);

	@Query("SELECT m.timestamp FROM Mensaje m WHERE m.type = :type "
			+ "AND m.sender = :sender AND m.dst = :dst AND m.msg = :msg ORDER BY m.timestamp DESC")
    public List<Long> findMsgDateBySender(@Param("type") String type, @Param("sender") String sender, @Param("msg") String msg,
    										@Param("dst") String dst);
}