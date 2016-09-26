package org.apache.storm.starter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.HdrHistogram.Histogram;
import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.ClusterSummary;
import org.apache.storm.generated.ExecutorSummary;
import org.apache.storm.generated.KillOptions;
import org.apache.storm.generated.Nimbus;
import org.apache.storm.generated.SpoutStats;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.generated.TopologyInfo;
import org.apache.storm.generated.TopologySummary;
import org.apache.storm.metric.HttpForwardingMetricsServer;
import org.apache.storm.metric.api.IMetricsConsumer.DataPoint;
import org.apache.storm.metric.api.IMetricsConsumer.TaskInfo;
import org.apache.storm.starter.bolt.IntermediateRankingsBolt;
import org.apache.storm.starter.bolt.RollingCountBolt;
import org.apache.storm.starter.bolt.TotalRankingsBolt;

import org.apache.storm.starter.spout.TrendingTopicSpout;
import org.apache.storm.starter.tools.Rankings;
import org.apache.storm.starter.util.StormRunner;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;

public class TrendingTopic {
	private static final Logger LOG = Logger.getLogger(TrendingTopic.class);
	  private static final int DEFAULT_RUNTIME_IN_SECONDS = 120;
	  private static final int TOP_N = 5;

	  private final TopologyBuilder builder;
	  private final String topologyName;
	  private final Config topologyConfig;
	  private final int runtimeInSeconds;
	  
	  public TrendingTopic(String topologyname, long rateperSecond, Config conf) throws InterruptedException{
		  	builder = new TopologyBuilder();
		    topologyName = topologyname;
		    topologyConfig = conf;
		    runtimeInSeconds = DEFAULT_RUNTIME_IN_SECONDS;

		    wireTopology(rateperSecond);
	  }	  

	  private void wireTopology(long rateperSecond) throws InterruptedException {
		    String spoutId = "TrendingTopicwithFrequency";
		    String counterId = "counter";
		    String intermediateRankerId = "intermediateRanker";
		    String totalRankerId = "finalRanker";
		    builder.setSpout(spoutId, new TrendingTopicSpout(false, rateperSecond), 5);
		    builder.setBolt(counterId, new RollingCountBolt(9, 3), 4).fieldsGrouping(spoutId, new Fields("word"));
		    builder.setBolt(intermediateRankerId, new IntermediateRankingsBolt(TOP_N), 4).fieldsGrouping(counterId, new Fields(
		        "obj"));
		    builder.setBolt(totalRankerId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerId);
		  }
		  
	  public static void main(String[] args) throws Exception {
		  
		  long ratePerSecond = 250;
		  int parallelism = 4;
	//      String name = "TrendingTopicwithSettingTime";
		 
	      Config conf = new Config();
	      
	      conf.setNumWorkers(parallelism);
	      conf.registerMetricsConsumer(org.apache.storm.metric.LoggingMetricsConsumer.class);
	      conf.put(Config.TOPOLOGY_WORKER_GC_CHILDOPTS,
	        "-XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:NewSize=128m -XX:CMSInitiatingOccupancyFraction=70 -XX:-CMSConcurrentMTEnabled");
	      conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, "-Xmx2g");
	      
	      
		  String topologyName = "TrendingTopicwithSettingTime";
		    if (args.length >= 1) {
		      topologyName = args[0];
		    }
		    boolean runLocally = true;
		    if (args.length >= 2 && args[1].equalsIgnoreCase("remote")) {
		      runLocally = false;
		    }

		    LOG.info("Topology name: " + topologyName);
		    TrendingTopic tt = new TrendingTopic(topologyName, ratePerSecond, conf);
		    if (runLocally) {
		      LOG.info("Running in local mode");
//		      LocalCluster cluster = new LocalCluster();
//		      cluster.submitTopology(topologyName, conf, tt.builder.createTopology());
		      StormRunner.runTopologyLocally(tt.builder.createTopology(), topologyName, tt.topologyConfig, tt.runtimeInSeconds);
		    }
		    else {
		      LOG.info("Running in remote (cluster) mode");
		   //   StormRunner.runTopologyRemotely(tt.builder.createTopology(), topologyName, tt.topologyConfig);
		      StormSubmitter.submitTopology(topologyName, conf, tt.builder.createTopology());
		    }
	  }	    
		
		     
	 
	  public static class SentWithTime {
	        public final String sentence;
	        public final long time;

	        public SentWithTime(String sentence, long time) {
	            this.sentence = sentence;
	            this.time = time;
	        }
	      }
	  
	
}
