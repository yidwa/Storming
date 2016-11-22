package benchmark;

import org.apache.log4j.Logger;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.starter.util.StormRunner;
import benchmark.LineTopology;


public class Benchmarking {
	private static final Logger LOG = Logger.getLogger(LineTopology.class);
	public static String topologyname;
	public static String freq;
	public static int numworker;
	
	
//	four parameters, name ,freq, numworkers
	public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		  
			boolean runLocally = true;
			topologyname = "line";
			 
		    if (args.length >= 2) {
		    	runLocally = false;
		    	topologyname = args[0];
		    	freq = args[1];
		    	if (!(Integer.valueOf(args[2])>0))
		    		numworker = 1;
		    	else 
		    		numworker = Integer.valueOf(args[2]);
		    }
		    else{ 
		    	freq = "1";	
		    	numworker = 1;
		    }
		   
		    LOG.info("Topology name: " + topologyname);
		  
		    BenchMarkTopology bt;
		    
		    if(topologyname.equalsIgnoreCase("diamond")){
		    	bt = new DiamondTopology(topologyname, numworker, Long.valueOf(freq));
		    }
		    else if(topologyname.equalsIgnoreCase("star")){
		    	bt = new StarTopology(topologyname, numworker, Long.valueOf(freq));
		    }

		    else {
		    	bt = new LineTopology(topologyname, numworker, Long.valueOf(freq));
		    }
		    
		    if (runLocally) {
		      LOG.info("Running in local mode");
		     
		      StormRunner.runTopologyLocally(bt.builder.createTopology(), topologyname, bt.topologyConfig, bt.runtimeInSeconds);
		    }
		    else {
		      LOG.info("Running in remote (cluster) mode");
		      StormRunner.runTopologyRemotely(bt.builder.createTopology(), topologyname, bt.topologyConfig);
		    }
	  	}
	
}
