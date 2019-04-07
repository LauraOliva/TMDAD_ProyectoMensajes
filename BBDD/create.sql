CREATE TABLE `chat`.`usuario` (
	`username` VARCHAR(45) NOT NULL,
	`password` VARCHAR(45) NOT NULL,
	`root` TINYINT NOT NULL,
	`activeroom` VARCHAR(45) NULL,
	PRIMARY KEY (`username`));
  
CREATE TABLE `chat`.`chatroom` (
	`idchatroom` INT NOT NULL AUTO_INCREMENT,
	`admin` VARCHAR(45) NULL,
	`multipleusers` TINYINT NOT NULL,
	`name` VARCHAR(45) NOT NULL,
	PRIMARY KEY (`idchatroom`));
	
CREATE TABLE `chat`.`mensajes` (
  `id_mensaje` INT NOT NULL AUTO_INCREMENT,
  `sender` VARCHAR(45) NOT NULL,
  `dst` VARCHAR(45) NOT NULL,
  `timestamp` DOUBLE NOT NULL,
  `msg` VARCHAR(500) NOT NULL,
  `type` VARCHAR(15) NOT NULL,
  PRIMARY KEY (`id_mensaje`));

