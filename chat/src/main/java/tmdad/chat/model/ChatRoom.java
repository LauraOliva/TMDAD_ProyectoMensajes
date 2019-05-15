package tmdad.chat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chatroom")
@NoArgsConstructor
/* Tabla chatroom de la base de datos chat */
public class Chatroom {
	
	@Id 
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
	@Getter @Setter
	private String idchatroom;
	
	@NotNull
	@Getter @Setter
	private String admin;
	
	@NotNull
	@Getter @Setter
	private boolean multipleusers;
	
	@NotNull
	@Getter @Setter
	private String name;

	public Chatroom(String admin, boolean multiple, String name) {
		this.admin=admin;
		this.multipleusers=multiple;
		this.name=name;
	}
}
