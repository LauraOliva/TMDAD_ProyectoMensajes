package tmdad.chat.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@AllArgsConstructor
@NoArgsConstructor
/* Tabla usuario de la base de datos chat */
public class Usuario{
	
	@Id @NotNull
	@Getter @Setter
	@Autowired
	private String username;
	
	@NotNull
	@Getter @Setter
	private boolean root;
	
	@Getter @Setter
	private String activeroom;

}
