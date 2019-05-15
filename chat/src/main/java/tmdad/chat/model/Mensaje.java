package tmdad.chat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mensajes")
@AllArgsConstructor
@NoArgsConstructor
/* Tabla mensajes de la base de datos chat */
public class Mensaje {
	@Id @NotNull
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Getter @Setter
	private int id_mensaje;
	
	@NotNull
	@Getter @Setter
	private String sender;
	
	@NotNull
	@Getter @Setter
	private String dst;
	
	@NotNull
	@Getter @Setter
	private long timestamp;
	
	@NotNull
	@Getter @Setter
	@Column(length = 500)
	private String msg;
	
	@NotNull
	@Getter @Setter
	private String type;

	public Mensaje(String sender, String dst, long timestamp, String msg, String type) {
		this.sender = sender;
		this.dst = dst;
		this.timestamp = timestamp;
		this.msg = msg;
		this.type = type;
	}
}
