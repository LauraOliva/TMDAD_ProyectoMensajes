package tmdad.chat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chatroom")
@NoArgsConstructor
public class Chatroom {
	
	@Id @NotNull
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Getter @Setter
	private int idchatroom;
	
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
