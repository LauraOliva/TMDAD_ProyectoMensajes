
package tmdad.chat.bbdd;

import java.sql.*;  

public class MySQLConnection {
	public static Connection con;
	public static void connect(){  
		try{  
			Class.forName("com.mysql.jdbc.Driver");  
			con = DriverManager.getConnection(  
					"jdbc:mysql://localhost:3306/chat","root","toor");  
		}catch(Exception e){ System.err.println(e);}  
	}  
	
	public static void close(){
		try {
			con.close();
		} catch (SQLException e) { System.err.println(e);}  
	}	
	
}
