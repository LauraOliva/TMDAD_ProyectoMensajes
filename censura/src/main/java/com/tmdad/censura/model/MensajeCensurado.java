package com.tmdad.censura.model;

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
public class MensajeCensurado {
	
	@Id @NotNull
	@Getter @Setter
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@NotNull
	@Getter @Setter
	private String usuario;
	
	@NotNull
	@Getter @Setter
	private String msg;
	
	@NotNull
	@Getter @Setter
	private String palabras;
	
	@NotNull
	@Getter @Setter
	private long timestamp;
	
	public MensajeCensurado(String u, String m, String p, long t){
		this.usuario = u;
		this.msg = m;
		this.palabras = p;
		this.timestamp = t;
	}

}
