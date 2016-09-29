package general;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StormREST {
	// all topologies 
	HashMap<String, String> topologies;
	ArrayList<Supervisor> workers;
	
	URL url;
	String hostport;
	HttpURLConnection conn;
	Object obj;
	JSONObject jobj;
	
	public StormREST(String hostport){
		this.hostport = hostport;
		topologies  = new HashMap<String, String>();
		this.obj = null;
		this.jobj = null;
		
	}
	
	public void Topologyget(){
		try{
			url = new URL(hostport+"/api/v1/topology/summary");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
		
			if (conn.getResponseCode() != 200){
				throw new RuntimeException("Failed : http error code"+ conn.getResponseCode());
			}
				
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output;
		//	System.out.println("output from server \n");
			while((output = br.readLine()) != null){
		//		System.out.println(output+ "\n");
				JSONParser parser = new JSONParser();
				obj = parser.parse(output);
				jobj = (JSONObject)obj;
				JSONArray topo = (JSONArray) jobj.get("topologies");
				for (int i = 0 ; i< topo.size(); i++){
					obj = topo.get(i);
					jobj = (JSONObject) obj;
//					System.out.println(temp.get("name")+ ", "+temp.get("id"));
					//System.out.println(topo.get(i).getClass().getName());
					
					topologies.put((String)jobj.get("name"), (String)jobj.get("id"));
				}

			}
		//	System.out.println("values " +topologies.toString());
			conn.disconnect();
		}
		catch (MalformedURLException e){
			e.printStackTrace();
		}
		catch (IOException e){
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void Supervisorinfo(){
		    workers = new ArrayList<Supervisor>();
			try {
				url = new URL(hostport+"/api/v1/supervisor/summary");
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
				if (conn.getResponseCode() != 200){
					throw new RuntimeException("Failed : http error code"+ conn.getResponseCode());
					}

				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String output;
				
				while((output = br.readLine()) != null){
					JSONParser parser = new JSONParser();
				//	System.out.println(output);
					obj = parser.parse(output);
					jobj = (JSONObject)obj;
					JSONArray topo = (JSONArray) jobj.get("supervisors");
//					System.out.println("size is "+topo.size());
					for (int i = 0 ; i< topo.size(); i++){
						Object tobj = topo.get(i);
						JSONObject tjobj = (JSONObject) tobj;
				//		System.out.println(tjobj.get("id"));
						Supervisor s = new Supervisor((String)tjobj.get("id"),(String)tjobj.get("host"),(Long)tjobj.get("slotsTotal"),
								(Long)tjobj.get("slotsUsed"),(Double)tjobj.get("totalMem"),(Double)tjobj.get("totalCpu"),
								(Double)tjobj.get("usedMem"),(Double)tjobj.get("usedCpu"));
//						workers.add(new Supervisor((String)tjobj.get("id"),(String)tjobj.get("host"),(Long)tjobj.get("slotsTotal"),
//								(Long)tjobj.get("slotsUsed"),(Double)tjobj.get("totalMem"),(Double)tjobj.get("totalCpu"),
//								(Double)tjobj.get("usedMem"),(Double)tjobj.get("usedCpu")));
					
						workers.add(s);
					}
					
					//System.out.println(output+ "\n");
					}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(Supervisor s : workers)
				System.out.println(s.toString());
			
			conn.disconnect();

		
	}
}

