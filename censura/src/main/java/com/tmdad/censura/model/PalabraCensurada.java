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
@Table(name = "palabras")
@AllArgsConstructor
@NoArgsConstructor
/* Tabla palabras de la base de datos de censura */
public class PalabraCensurada{
	
	@Id @NotNull
	@Getter @Setter
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@NotNull
	@Getter @Setter
	private String palabra;
	
	@NotNull
	@Getter @Setter
	private long timestamp;
	
	public PalabraCensurada(String p, long t){
		this.palabra = p;
		this.timestamp = t;
	}

}
