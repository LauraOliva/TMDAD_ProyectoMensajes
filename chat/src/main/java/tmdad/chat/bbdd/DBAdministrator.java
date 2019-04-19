package tmdad.chat.bbdd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DBAdministrator {
	private Connection con;
	
	public DBAdministrator() {
		MySQLConnection.connect();
		con = MySQLConnection.con;
	}
	
	/* TABLA USUARIO */
	
	public void insertUser(String username, String pass, boolean root){
		String query = "INSERT INTO usuario (username, password, root, activeroom) "
				+"VALUES ('" + username + "','" + pass + "'," + root + ",null)";
		Statement st;
		try {
			st = con.createStatement();
			st.executeUpdate(query);
			st.close();
		} catch(SQLException e){ System.err.println(e);}  
	}
	
	public void removeUser(String username){
		String query = "DELETE FROM usuario WHERE username='" + username + "';";
		Statement st;
		try {
			st = con.createStatement();
		    st.executeUpdate(query);
		    st.close();
		} catch(SQLException e){ System.err.println(e);}  
	}

	public boolean existsUser(String username){
		String query = "SELECT * FROM usuario WHERE username='" + username + "';";
		Statement st;
		ResultSet rs;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
			boolean result = rs.next();
			st.close();
			return result;
		} catch(SQLException e){ System.err.println(e);}  
		return true;
	}
	
	public boolean verifyUser(String username, String pass){
		String query = "SELECT * FROM usuario WHERE username='" + username + "'"
				+ "AND password='" + pass + "';";
		Statement st;
		ResultSet rs;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
			boolean result = rs.next();
			st.close();
			return result;
		} catch(SQLException e){ System.err.println(e);}  
		return false;
	}
	
	public void setActiveRoom(String username, String id_room){
		String query = "UPDATE usuario SET activeroom='" + id_room 
				+ "' WHERE username='" + username + "';";
		Statement st;
		try {
			st = con.createStatement();

			st.executeUpdate(query);
			st.close();
		} catch(SQLException e){ System.err.println(e);}  
	}
	
	public void removeActiveRoom(String username){
		String query = "UPDATE usuario SET activeroom=null WHERE username='" 
				+ username + "';";
		Statement st;
		try {
			st = con.createStatement();

			st.executeUpdate(query);
			st.close();
		} catch(SQLException e){ System.err.println(e);}  
	}
	
	public String getActiveRoom(String username){
		String query = "SELECT activeroom FROM usuario WHERE username='" + username + "';";
		Statement st;
		ResultSet rs;
		String id_room = "";
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
		    while (rs.next()){
		    	id_room = rs.getString("activeroom");
		    }
		} catch(SQLException e){ System.err.println(e);}  
    	System.out.println("Chat activo " + id_room);
	    return id_room;
	}
	
	public boolean isRoot(String username){
		String query = "SELECT root FROM usuario WHERE username='" + username + "';";
		Statement st;
		ResultSet rs;
		boolean is_root = false;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
		    while (rs.next()){
		    	is_root = rs.getBoolean("root");
		    }
		} catch(SQLException e){ System.err.println(e);}  
    	System.out.println("Es asmin " + is_root);
	    return is_root;
	}
	
	public ArrayList<String> getUsersChat(String id_activeChat){
		ArrayList<String> users = new ArrayList<>();
		String query = "SELECT username FROM usuario WHERE activeroom = '" 
				+ id_activeChat + "';";
		Statement st;
		ResultSet rs;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
		    while (rs.next()){
		    	String u = rs.getString("username");
		    	users.add(u);
		    }
		} catch(SQLException e){ System.err.println(e);}  
		return users;
	}
	
	/* TABLA CHATROOM */
	
	public void insertChat(String name, String admin, boolean multiple){
		String query = "INSERT INTO chatroom (name, admin, multipleusers) "
				+"VALUES ('" + name + "','" + admin + "'," + multiple + ");";
		Statement st;
		try {
			st = con.createStatement();
			st.executeUpdate(query);
			st.close();
		} catch(SQLException e){ System.err.println(e);}  
	}

	public boolean existsChat(String name, boolean multiple){
		String query = "SELECT * FROM chatroom WHERE name='" + name + "' AND multipleusers=" + multiple + ";";
		Statement st;
		ResultSet rs;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
			boolean result = rs.next();
			st.close();
			return result;
		} catch(SQLException e){ System.err.println(e);}  
		return true;
	}
	
	public void removeChat(String name){
		String query = "DELETE FROM chatroom WHERE name='" + name + "';";
		Statement st;
		try {
			st = con.createStatement();
		    st.executeUpdate(query);
		    st.close();
		} catch(SQLException e){ System.err.println(e);}  
	}

	public boolean isAdmin(String name, String username){
		String query = "SELECT admin FROM chatroom WHERE name='" + name + "';";
		Statement st;
		ResultSet rs;
		String admin = "";
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
		    while (rs.next()){
		    	admin = rs.getString("admin");
		    }
		    if(admin.equals(username)) return true;
		} catch(SQLException e){ System.err.println(e);}  
	    return false;
	}

	public boolean isMultiple(String name){
		String query = "SELECT multipleusers FROM chatroom WHERE name='" + name + "';";
		Statement st;
		ResultSet rs;
		boolean multiple = false;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
		    while (rs.next()){
		    	multiple = rs.getBoolean("multipleusers");
		    }
		} catch(SQLException e){ System.err.println(e);}  
	    return multiple;
	}
	
	/* TODO que funcione por fechas */
	/* TODO otra opcin es añadir separar el campo de activechat en 2: chat (string) y activechat (bool) */
	public boolean isUserInChat(String username, String name){
		String query = "SELECT * FROM usuario WHERE username='" + username 
				+ "' AND activeroom='" + name + "';";
		System.out.println(query);
		Statement st;
		ResultSet rs;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
			boolean result = rs.next();
			st.close();
			return result;
		} catch(SQLException e){ System.err.println(e);}  
		return false;
	}
	
	/* TABLA MENSAJE */
	
	public void insertMsg(String sender, String dst, long timestamp, String msg, String type){
		String query = "INSERT INTO mensajes (sender, dst, timestamp, msg, type) "
				+"VALUES ('" + sender + "','" + dst + "'," + timestamp + ",'" + msg + "','" + type + "');";
		Statement st;
		try {
			st = con.createStatement();
			st.executeUpdate(query);
			st.close();
		} catch(SQLException e){ System.err.println(e);}  
	}

	public ArrayList<String> getMsg(String id, String type, String username, boolean multiple){
		ArrayList<String> msgs = new ArrayList<>();
		
		String query = "SELECT sender, timestamp, msg FROM mensajes, chatroom WHERE type='"+ type 
				+ "' AND dst='" + id + "'";
		
		if(type.equals("chat")){
			query = query + " AND name=dst AND multipleusers=" + multiple;
			if(multiple){
				long time = getDateJoin(username, id);
				query = query + " AND timestamp >= " + time;
			}
		}
		
		query = query + " ORDER BY timestamp;";
		System.out.println(query);
		Statement st;
		ResultSet rs;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
		    while (rs.next()){
		    	String s = rs.getString("sender");
		    	long t = rs.getLong("timestamp");
		    	String m = rs.getString("msg");
		    	String timestamp = new SimpleDateFormat("HH:mm").format(t);	
		    	String msg = "<b>" + s + ":</b> " + m + " (" + timestamp + ")";
		    	msgs.add(msg);
		    }
		} catch(SQLException e){ System.err.println(e);}  
		
		return msgs;
	}
	
	
	/* TODO */
	public void removeMsgRoom(String id_room){
		String query = "DELETE FROM mensajes WHERE type='chat' AND dst='" + id_room + "';";
		Statement st;
		try {
			st = con.createStatement();
		    st.executeUpdate(query);
		    st.close();
		} catch(SQLException e){ System.err.println(e);}  
	}
	
	public long getDateJoin(String username, String id_room){
		String query = "SELECT timestamp FROM chat.mensajes WHERE type='notification' AND dst='"
				+ username + "' AND msg ='Te has unido a la sala " + id_room 
				+ "' ORDER BY timestamp DESC LIMIT 1;";
		Statement st;
		ResultSet rs;
		long time = 0;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query);
		    while (rs.next()){
		    	time = rs.getLong("timestamp");
		    }
		} catch(SQLException e){ System.err.println(e);}  
	    return time;
	}
	
	public boolean hasBeenInvited(String username, String id_room){
		String query_inv = "SELECT timestamp FROM chat.mensajes WHERE type='notification' AND dst='"
				+ username + "' AND msg ='Has sido invitado a la sala " + id_room + " (JOINROOM " + id_room + " para aceptar)" 
				+ "' ORDER BY timestamp DESC LIMIT 1;";
		
		String query_leave = "SELECT timestamp from mensajes WHERE type='notification' AND "
				+ "(msg='Has abandonado la sala " + id_room + "' OR msg='Has sido expulsado de la sala " 
				+ id_room + "') ORDER BY timestamp DESC LIMIT 1;";
		
		System.out.println(query_inv);
		System.out.println(query_leave);
		
		/* TODO comprobar que la fecha de la invitacion sea posterior a la fecha en la que haya abandonado el grupo en caso en el que lo haya abandonado */
		Statement st;
		ResultSet rs;
		long time_inv = 0, time_leave = 0;
		try {
			st = con.createStatement();
			// execute the query, and get a java resultset
			rs = st.executeQuery(query_inv);
		    while (rs.next()){
		    	time_inv = rs.getLong("timestamp");
		    }
		    rs = st.executeQuery(query_leave);
		    while (rs.next()){
		    	time_leave = rs.getLong("timestamp");
		    }
		} catch(SQLException e){ System.err.println(e);}  
		if (time_inv == 0) return false;
		else if(time_inv != 0 && time_leave == 0) return true;
		else if(time_inv < time_leave) return false;
		else if(time_inv >= time_leave) return true;
		else return false;
	}
	
	
}
