package benchmark;

import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.starter.TrendingTopic;
import org.apache.storm.starter.bolt.IntermediateRankingsBolt;
import org.apache.storm.starter.bolt.RollingCountBolt;
import org.apache.storm.starter.bolt.TotalRankingsBolt;
import org.apache.storm.starter.spout.TrendingTopicSpout;
import org.apache.storm.starter.util.StormRunner;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class ChainTopology {
	  private static final Logger LOG = Logger.getLogger(TrendingTopic.class);
	  private static final int DEFAULT_RUNTIME_IN_SECONDS = 30;
	  private final TopologyBuilder builder;
	  private final String topologyName;
	  private final Config topologyConfig;
	  private final int runtimeInSeconds;
	  public static String freq;
	  public static int numworker;

	  
//	  public ChainTopology(String topologyname, int numworker, long rateperSecond) throws InterruptedException{
//		  	builder = new TopologyBuilder();
//		    topologyName = topologyname;
//		    topologyConfig = createTopologyConfiguration();
//		    runtimeInSeconds = DEFAULT_RUNTIME_IN_SECONDS;
//		    ChainTopology.numworker = numworker;
////		    ChainTopology.freq = "1000";
//		    wireTopology(rateperSecond);
//		    
//	  }
	  
	  
	  public ChainTopology(String topologyname, long rateperSecond) throws InterruptedException{
		  	builder = new TopologyBuilder();
		    topologyName = topologyname;
		    topologyConfig = createTopologyConfiguration();
		    runtimeInSeconds = DEFAULT_RUNTIME_IN_SECONDS;
		    
		    wireTopology(rateperSecond);
	  }
	  
	  private static Config createTopologyConfiguration() {
		    Config conf = new Config();
		    conf.setDebug(true);
		    conf.setNumWorkers(numworker);
		    return conf;
		  }
	  
	  private void wireTopology(long rateperSecond) throws InterruptedException {
		  //  String spoutId = "Spouting";
		    String aId = "appending_a";
		    
		    
		    String spoutId = "TrendingTopicwithFrequency";
		    String counterId = "counter";
		    String intermediateRankerId = "intermediateRanker";
		    String totalRankerId = "finalRanker";
		    
		    
//		    String intermediateRankerId = "intermediateRanker";
//		    String totalRankerId = "finalRanker";
		//    int p = Constants.parallel;
		//    builder.setSpout(spoutId, new Spout(false, rateperSecond));
//		    builder.setSpout(spoutId, new TrendingTopicSpout(false, rateperSecond));
//		    builder.setBolt(aId, new B_appendA(),2).fieldsGrouping(spoutId, new Fields("word"));
//		    builder.setBolt(aId, new B_appendA(),2);
//		    builder.setBolt(aId, new RollingCountBolt(9, 3), 3).fieldsGrouping(spoutId, new Fields("word"));
		    
		    builder.setSpout(spoutId, new TrendingTopicSpout(false, rateperSecond),2);
		    builder.setBolt(counterId, new RollingCountBolt(9, 3), 3).fieldsGrouping(spoutId, new Fields("word"));
		    builder.setBolt(intermediateRankerId, new IntermediateRankingsBolt(4), 2).fieldsGrouping(aId, new Fields(
			        "obj"));
			builder.setBolt(totalRankerId, new TotalRankingsBolt(4)).globalGrouping(intermediateRankerId);
			
			
//		    builder.setBolt(intermediateRankerId, new IntermediateRankingsBolt(TOP_N), 2).fieldsGrouping(counterId, new Fields(
//		        "obj"));
//		    builder.setBolt(totalRankerId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerId);
		  }
	  
		public static class SentWithTime{
			public final String sending;
			public final long time;
			public SentWithTime(String sentence, long time){
				this.sending = sentence;
				this.time = time;
			}
		}
		public static void main(String[] args) throws NumberFormatException, InterruptedException, AlreadyAliveException, InvalidTopologyException, AuthorizationException {
				String name = "benchmark_chain";
				 
			      String topologyName = "benchmark_chain";
				    if (args.length >= 1) {
				      topologyName = args[0];
				    }
				    else 
				    	freq = "1";	
				    boolean runLocally = true;
				    if (args.length >= 2 && args[1].equalsIgnoreCase("remote")) {
				    	runLocally = false;
				    	freq = args[2];
				    }

				    LOG.info("Topology name: " + topologyName);
//				    ChainTopology ct = new ChainTopology(topologyName, 1, 1);
				    ChainTopology ct = new ChainTopology(topologyName, 1);
				    if (runLocally) {
				      LOG.info("Running in local mode");
				    
				      StormTopology st = ct.builder.createTopology();
				      System.out.println("topology built");
				      StormRunner.runTopologyLocally(st, topologyName, ct.topologyConfig, 30);
				      System.out.println("finished");
				    }
				    else {
				      LOG.info("Running in remote (cluster) mode");
				  
				      StormRunner.runTopologyRemotely(ct.builder.createTopology(), topologyName, ct.topologyConfig);
				    }
			}
}
