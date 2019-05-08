package tmdad.chat.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CensureAdapter {

	private static final String USER_AGENT = "Mozilla/5.0";
	@Value("${censura.url}")
	private String URL;
	@Value("${censura.url.add}")
	private String URL_ADD;
	@Value("${censura.url.remove}")
	private String URL_REMOVE;
	@Value("${censura.url.get}")
	private String URL_GET;
	@Value("${censura.url.filter}")
	private String URL_FILTER;
	
	public String doGet(String url, String failure) throws IOException{
		URL obj = new URL(url);
		System.err.println(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			return response.toString();
		} else {
			return failure;
		}
	}
	
	public String doPost(String url, String params, String failure) throws IOException{
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);

		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(params.getBytes());
		os.flush();
		os.close();
		// For POST only - END

		int responseCode = con.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			return response.toString();
		} else {
			return failure;
		}
	
	}
	
	public String addWord(String word){
		String params = "word=" + word;
		System.err.println(URL+URL_ADD);
		try {
			return doPost(URL+URL_ADD, params, "No se ha podido añadir la palabra " + word);
		} catch (IOException e) {
			return "No se ha podido añadir la palabra " + word;
		}
	}
	
	public String removeWord(String word){
		String params = "word=" + word;
		try {
			return doPost(URL+URL_REMOVE, params, "No se ha podido eliminar la palabra " + word);
		} catch (IOException e) {
			return "No se ha podido eliminar la palabra " + word;
		}
	}
	
	public ArrayList<String> filterMsg(String msg, String sender){

		String params = "msg=" + msg + "&sender=" + sender;
		ArrayList<String> result = new ArrayList<>();
		try {
			String r = doPost(URL+URL_FILTER, params, null);
			if(r == null){
				result.add("false");
			}
			else{
				result.add("true");
				result.add(r);
			}
		} catch (IOException e) {
			result.add("false");
		}
		return result;
	}
	
	public String censuredWords(){
		try {
			return doGet(URL+URL_GET, "No se han podido recuperar las palabras censuradas");
		} catch (IOException e) {
			return "No se han podido recuperar las palabras censuradas";
		}
	}
}
