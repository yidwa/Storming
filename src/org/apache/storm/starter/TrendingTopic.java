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
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.ClusterSummary;
import org.apache.storm.generated.ExecutorSummary;
import org.apache.storm.generated.InvalidTopologyException;
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

import general.Constants;

public class TrendingTopic {
	private static final Logger LOG = Logger.getLogger(TrendingTopic.class);
	  private static final int DEFAULT_RUNTIME_IN_SECONDS = 60;
	  private static final int TOP_N = 5;

	  private final TopologyBuilder builder;
	  private final String topologyName;
	  private final Config topologyConfig;
	  private final int runtimeInSeconds;
	  public static String freq;
	  public static String parallel;
	 
	  public TrendingTopic(String topologyname, long rateperSecond, int parallel) throws InterruptedException{
		  	builder = new TopologyBuilder();
		    topologyName = topologyname;
		    topologyConfig = createTopologyConfiguration();
		    runtimeInSeconds = DEFAULT_RUNTIME_IN_SECONDS;
		    
		    wireTopology(rateperSecond, parallel);
	  }
	  
	  private static Config createTopologyConfiguration() {
		    Config conf = new Config();
		    conf.setDebug(true);
		    return conf;
		  }

	  private void wireTopology(long rateperSecond, int parallel) throws InterruptedException {
		    String spoutId = "TrendingTopicwithFrequency";
		    String counterId = "counter";
		    String intermediateRankerId = "intermediateRanker";
		    String totalRankerId = "finalRanker";
		//    int p = Constants.parallel;
		    builder.setSpout(spoutId, new TrendingTopicSpout(false, rateperSecond));
		    builder.setBolt(counterId, new RollingCountBolt(9, 3), parallel).fieldsGrouping(spoutId, new Fields("word"));
		    builder.setBolt(intermediateRankerId, new IntermediateRankingsBolt(TOP_N), parallel).fieldsGrouping(counterId, new Fields(
		        "obj"));
		    builder.setBolt(totalRankerId, new TotalRankingsBolt(TOP_N), parallel).globalGrouping(intermediateRankerId);
		  }
		  
	  
	  
	  public static class SentWithTime {
		    public final String sentence;
		    public final long time;

		    public SentWithTime(String sentence, long time) {
		        this.sentence = sentence;
		        this.time = time;
		    }
		    
	  }
	  public static void main(String[] args) throws InterruptedException, AlreadyAliveException, InvalidTopologyException, AuthorizationException{
		  
		  parallel = args[2];
		  freq = args[3];
		  
		  
		 // long ratePerSecond = Constants.freq;
		
	      String name = "TrendingTopicwithSettingTime";
		 
	      String topologyName = "TrendingTopicwithSettingTime";
		    if (args.length >= 1) {
		      topologyName = args[0];
		    }
		    boolean runLocally = true;
		    if (args.length >= 2 && args[1].equalsIgnoreCase("remote")) {
		      runLocally = false;
		    }

		    LOG.info("Topology name: " + topologyName);
		    TrendingTopic tt = new TrendingTopic(topologyName,Long.valueOf(freq), Integer.valueOf(parallel));
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
	 //     Config conf = new Config();
//	     HttpForwardingMetricsServer metricServer = new HttpForwardingMetricsServer(conf) {
//	          @Override
//	          public void handle(TaskInfo taskInfo, Collection<DataPoint> dataPoints) {
//	              String worker = taskInfo.srcWorkerHost + ":" + taskInfo.srcWorkerPort;
//	              for (DataPoint dp: dataPoints) {
//	                  if ("comp-lat-histo".equals(dp.name) && dp.value instanceof Histogram) {
//	                      synchronized(_histo) {
//	                          _histo.add((Histogram)dp.value);
//	                      }
//	                  } else if ("CPU".equals(dp.name) && dp.value instanceof Map) {
//	                     Map<Object, Object> m = (Map<Object, Object>)dp.value;
//	                     Object sys = m.get("sys-ms");
//	                     if (sys instanceof Number) {
//	                         _systemCPU.getAndAdd(((Number)sys).longValue());
//	                     }
//	                     Object user = m.get("user-ms");
//	                     if (user instanceof Number) {
//	                         _userCPU.getAndAdd(((Number)user).longValue());
//	                     }
//	                  } else if (dp.name.startsWith("GC/") && dp.value instanceof Map) {
//	                     Map<Object, Object> m = (Map<Object, Object>)dp.value;
//	                     Object count = m.get("count");
//	                     if (count instanceof Number) {
//	                         _gcCount.getAndAdd(((Number)count).longValue());
//	                     }
//	                     Object time = m.get("timeMs");
//	                     if (time instanceof Number) {
//	                         _gcMs.getAndAdd(((Number)time).longValue());
//	                     }
//	                  } else if (dp.name.startsWith("memory/") && dp.value instanceof Map) {
//	                     Map<Object, Object> m = (Map<Object, Object>)dp.value;
//	                     Object val = m.get("usedBytes");
//	                     if (val instanceof Number) {
//	                         MemMeasure mm = _memoryBytes.get(worker);
//	                         if (mm == null) {
//	                           mm = new MemMeasure();
//	                           MemMeasure tmp = _memoryBytes.putIfAbsent(worker, mm);
//	                           mm = tmp == null ? mm : tmp; 
//	                         }
//	                         mm.update(((Number)val).longValue());
//	                     }
//	                  }
//	              }
//	          }
//	      };
		  
	  //    metricServer.serve();
	    //  String url = metricServer.getUrl();
//	      
	      
	    //  Cluster cluster = new Cluster(conf);
//	      conf.setNumWorkers(parallelism);
	//      conf.registerMetricsConsumer(org.apache.storm.metric.LoggingMetricsConsumer.class);
	  //    conf.registerMetricsConsumer(org.apache.storm.metric.HttpForwardingMetricsConsumer.class, url, 1);
//	      Cluster cluster = new Cluster(conf);
//	      
	      
//	      Map<String, String> workerMetrics = new HashMap<String, String>();
//	      if (!cluster.isLocal()) {
//	        //sigar uses JNI and does not work in local mode
//	        workerMetrics.put("CPU", "org.apache.storm.metrics.sigar.CPUMetric");
//	      }
//	      conf.put(Config.TOPOLOGY_WORKER_METRICS, workerMetrics);
//	      conf.put(Config.TOPOLOGY_BUILTIN_METRICS_BUCKET_SIZE_SECS, 10);
//	      conf.put(Config.TOPOLOGY_WORKER_GC_CHILDOPTS,
//	        "-XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:NewSize=128m -XX:CMSInitiatingOccupancyFraction=70 -XX:-CMSConcurrentMTEnabled");
//	      conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, "-Xmx2g");
//	      
	      
		 
		    
		
		       
//		     for (int i = 0; i < numMins * 2; i++) {
//		            Thread.sleep(30 * 1000);
//		            writeFile("trying to write at "+ i+ " times");
//		            printMetrics(cluster, topologyName);
//		        }
	



	