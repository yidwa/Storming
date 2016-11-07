package benchmark;

import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.starter.util.StormRunner;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class StarTopology {
	private static final Logger LOG = Logger.getLogger(StarTopology.class);
	  private static final int DEFAULT_RUNTIME_IN_SECONDS = 30;
//	  private static final int TOP_N = 5;

	  private final TopologyBuilder builder;
	  private final String topologyName;
	  private final Config topologyConfig;
	  private final int runtimeInSeconds;
	  public static String freq;
//	  public static String parallel;
	 
	  public StarTopology(String topologyname, int numworkers, long rateperSecond) throws InterruptedException{
		  	builder = new TopologyBuilder();
		    topologyName = topologyname;
		    topologyConfig = createTopologyConfiguration(numworkers);
		    runtimeInSeconds = DEFAULT_RUNTIME_IN_SECONDS;
		    wireTopology(rateperSecond);
	  }
	  
	  private static Config createTopologyConfiguration(int numworkers) {
		    Config conf = new Config();
		    conf.setDebug(true);
		    conf.setNumWorkers(numworkers);
		    return conf;
		  }

	  private void wireTopology(long rateperSecond) throws InterruptedException {
		  	String spoutId = "Spouting";
		    String aId = "appending_a";
		    String bId = "appending_b";
		    String cId = "appending_c";
		    String dId = "appending_d";
		    String lId = "removelast";
		    builder.setSpout(spoutId, new Spout(false, rateperSecond));
		    builder.setBolt(aId, new Star_appendA(),2).fieldsGrouping(spoutId, new Fields("word"));
//		    builder.setBolt(aId, new B_appendA(),2).shuffleGrouping(spoutId, "addingb");
		    builder.setBolt(bId, new B_appendB(),2).shuffleGrouping(aId, "addingb");
		    builder.setBolt(cId, new B_appendC(),2).shuffleGrouping(aId, "addingc");
		    builder.setBolt(dId, new B_appendD(),2).shuffleGrouping(aId, "addingd");
//		    builder.setBolt(bId, new B_appendB(),2).fieldsGrouping(aId, new Fields("appenda"));
//		    builder.setBolt(cId, new B_appendC(),2).fieldsGrouping(aId, new Fields("appenda"));
//		    builder.setBolt(dId, new B_appendD(),2).fieldsGrouping(aId, new Fields("appenda"));
//		    builder.setBolt(lId, new B_removelast(),2).fieldsGrouping(aId, new Fields("appenda"));
		    builder.setBolt(lId, new B_removelast(),2).shuffleGrouping(aId, "removea");
//		    String spoutId = "TrendingTopicwithFrequency";
//		    String counterId = "counter";
//		    String intermediateRankerId = "intermediateRanker";
//		    String totalRankerId = "finalRanker";s
		//    int p = Constants.parallel;
//		    builder.setSpout(spoutId, new TrendingTopicSpout(false, rateperSecond),2);
//		    builder.setBolt(counterId, new RollingCountBolt(9, 3), 3).fieldsGrouping(spoutId, new Fields("word"));
//		    builder.setBolt(intermediateRankerId, new IntermediateRankingsBolt(TOP_N), 2).fieldsGrouping(counterId, new Fields(
//		        "obj"));
//		    builder.setBolt(totalRankerId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerId);
		    
		  }
		  
	  
	  
	  public static class SentWithTime {
		    public final String sentence;
		    public final long time;

		    public SentWithTime(String sentence, long time) {
		        this.sentence = sentence;
		        this.time = time;
		    }
		    
	  }
	  

	  
	  
	  // four parameters, name , remote, freq, numworkers
	  public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException, AuthorizationException{
		  
		  int numworker = 0;
		 
	      String topologyName = "star_benchmark";
		    if (args.length >= 1) {
		      topologyName = args[0];
		    }
		    else{ 
		    	freq = "1";	
		    	numworker = 1;
		    }
		    boolean runLocally = true;
		    if (args.length >= 2 && args[1].equalsIgnoreCase("remote")) {
		    	runLocally = false;
		    	freq = args[2];
		    	if (!(Integer.valueOf(args[3])>0))
		    		numworker = 1;
		    	else 
		    		numworker = Integer.valueOf(args[3]);
		    }

		    LOG.info("Topology name: " + topologyName);
		    StarTopology st = new StarTopology(topologyName, numworker, Long.valueOf(freq));
		    if (runLocally) {
		      LOG.info("Running in local mode");
		      StormRunner.runTopologyLocally(st.builder.createTopology(), topologyName, st.topologyConfig, st.runtimeInSeconds);
		    }
		    else {
		      LOG.info("Running in remote (cluster) mode");
		      StormRunner.runTopologyRemotely(st.builder.createTopology(), topologyName, st.topologyConfig);
		    }
	  	}
	 }
	