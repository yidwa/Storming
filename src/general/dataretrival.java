package general;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class dataretrival {

//	HashMap<String, String> topo;
//	
//	public dataretrival(){
//		this.topo = new HashMap<>();
//	}
//	public static void createJson(){
//		
//		JSONObject obj = new JSONObject();
//		obj.put("name", "yidan");
//		obj.put("age", new Integer(28));
//		
//		JSONArray list = new JSONArray();
//		list.add("msg 1");
//		list.add("msg 2");
//		list.add("msg 3");
//		
//		obj.put("message", list);
//		
//		try{
//			
//			FileWriter file = new FileWriter(new File("/Users/yidwa/Desktop/testing.json"));
//			file.write(obj.toJSONString());
//			file.flush();
//			file.close();
//		}
//		catch (IOException e){
//			e.printStackTrace();
//		}
//		
//		System.out.println(obj);
//		
//	}
	
//	public static void readJson(){
//		JSONParser parser = new JSONParser();
//		
//		try{
//			Object obj = parser.parse(new FileReader("/Users/yidwa/Desktop/testing.json"));
//			
//			JSONObject jo = (JSONObject) obj;
//			
//			String name = (String) jo.get("name");
//			System.out.println(name);
//			
//			long age = (Long) jo.get("age");
//			System.out.println(age);
//			
//			JSONArray msg = (JSONArray) jo.get("message");
//			Iterator<String> it = msg.iterator();
//			while(it.hasNext()){
//				System.out.println(it.next());
//			}
//			
//		}
//		catch (FileNotFoundException e){
//			e.printStackTrace();
//		}
//		catch (IOException e){
//			e.printStackTrace();
//		}
//		catch (ParseException e){
//			e.printStackTrace();
//		}
//	}

	
	public static void main(String[] args) {
	StormREST sr = new StormREST("http://115.146.85.187:8080");
	//sr.Topologyget();
	sr.Supervisorinfo();
	}
	
	
}


