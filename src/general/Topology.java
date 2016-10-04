package general;

import java.util.HashMap;

public class Topology {
	HashMap<String, Long> tworker;
	String id;
	
	public Topology(String id){
		this.id = id;
		this.tworker = new HashMap<String, Long>();
	}

	public HashMap<String, Long> getTworker() {
		return tworker;
	}

}
