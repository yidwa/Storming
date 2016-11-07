package benchmark;

import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.topology.TopologyBuilder;

public class BenchMarkTopology {
	  private static final Logger LOG = Logger.getLogger(BenchMarkTopology.class);
	  private static final int DEFAULT_RUNTIME_IN_SECONDS = 30;
	  final TopologyBuilder builder;
	  private final String topologyName;
	  final Config topologyConfig;
	  final int runtimeInSeconds;
	  public static String freq;
	  

	  public BenchMarkTopology(String topologyname, int numworkers, long rateperSecond) throws InterruptedException{
		  builder = new TopologyBuilder();
		  this.topologyName = topologyname;
		  this.runtimeInSeconds = DEFAULT_RUNTIME_IN_SECONDS;
		  this.topologyConfig = createTopologyConfiguration(numworkers);
	  }
	  
	  public Config createTopologyConfiguration(int numworkers) {
		    Config conf = new Config();
		    conf.setDebug(true);
		    conf.setNumWorkers(numworkers);
		    return conf;
		  }
}