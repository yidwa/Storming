package benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.starter.util.StormRunner;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import general.Methods;

public class LineTopology {
	private static final Logger LOG = Logger.getLogger(LineTopology.class);
	  private static final int DEFAULT_RUNTIME_IN_SECONDS = 30;
//	  private static final int TOP_N = 5;

	  private final TopologyBuilder builder;
	  private final String topologyName;
	  private final Config topologyConfig;
	  private final int runtimeInSeconds;
	  public static String freq;
//	  public static String parallel;
	 
	  public LineTopology(String topologyname, int numworkers, long rateperSecond) throws InterruptedException{
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
		    builder.setSpout(spoutId, new Spout(false, rateperSecond));
		    builder.setBolt(aId, new B_appendA(),2).fieldsGrouping(spoutId, new Fields("word"));
		    builder.setBolt(bId, new B_appendB(),2).fieldsGrouping(aId, new Fields("appendinga"));
		    builder.setBolt(cId, new B_appendC(),2).fieldsGrouping(bId, new Fields("appendingb"));
//		    String spoutId = "TrendingTopicwithFrequency";
//		    String counterId = "counter";
//		    String intermediateRankerId = "intermediateRanker";
//		    String totalRankerId = "finalRanker";
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
	  
	  public static void writeFile(String sen){
			try {
				String path = "/Users/yidwa/Desktop/Records.txt";
				File f = new File(path);
				FileWriter fw = new FileWriter(f,true);
				String time = Methods.formattime();
				fw.write(time+" , "+ sen+"\n");
			
				fw.flush();
					
				fw.close();
				}
				catch (IOException e1) {
						// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}
	  
	  
	  // four parameters, name , remote, freq, numworkers
	  public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException, AuthorizationException{
		  
		  int numworker = 0;
		 
	      String topologyName = "line_benchmark";
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
		    LineTopology tt = new LineTopology(topologyName, numworker, Long.valueOf(freq));
		    if (runLocally) {
		      LOG.info("Running in local mode");
		      StormRunner.runTopologyLocally(tt.builder.createTopology(), topologyName, tt.topologyConfig, tt.runtimeInSeconds);
		    }
		    else {
		      LOG.info("Running in remote (cluster) mode");
		      StormRunner.runTopologyRemotely(tt.builder.createTopology(), topologyName, tt.topologyConfig);
		    }
	  	}
	 }
	