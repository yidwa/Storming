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
		  
		  long ratePerSecond = 500;
		  int parallelism = 4;
		  int numMins = 2;
	      String name = "TrendingTopicwithSettingTime";
		 
	      Config conf = new Config();
	      HttpForwardingMetricsServer metricServer = new HttpForwardingMetricsServer(conf) {
	          @Override
	          public void handle(TaskInfo taskInfo, Collection<DataPoint> dataPoints) {
	              String worker = taskInfo.srcWorkerHost + ":" + taskInfo.srcWorkerPort;
	              for (DataPoint dp: dataPoints) {
	                  if ("comp-lat-histo".equals(dp.name) && dp.value instanceof Histogram) {
	                      synchronized(_histo) {
	                          _histo.add((Histogram)dp.value);
	                      }
	                  } else if ("CPU".equals(dp.name) && dp.value instanceof Map) {
	                     Map<Object, Object> m = (Map<Object, Object>)dp.value;
	                     Object sys = m.get("sys-ms");
	                     if (sys instanceof Number) {
	                         _systemCPU.getAndAdd(((Number)sys).longValue());
	                     }
	                     Object user = m.get("user-ms");
	                     if (user instanceof Number) {
	                         _userCPU.getAndAdd(((Number)user).longValue());
	                     }
	                  } else if (dp.name.startsWith("GC/") && dp.value instanceof Map) {
	                     Map<Object, Object> m = (Map<Object, Object>)dp.value;
	                     Object count = m.get("count");
	                     if (count instanceof Number) {
	                         _gcCount.getAndAdd(((Number)count).longValue());
	                     }
	                     Object time = m.get("timeMs");
	                     if (time instanceof Number) {
	                         _gcMs.getAndAdd(((Number)time).longValue());
	                     }
	                  } else if (dp.name.startsWith("memory/") && dp.value instanceof Map) {
	                     Map<Object, Object> m = (Map<Object, Object>)dp.value;
	                     Object val = m.get("usedBytes");
	                     if (val instanceof Number) {
	                         MemMeasure mm = _memoryBytes.get(worker);
	                         if (mm == null) {
	                           mm = new MemMeasure();
	                           MemMeasure tmp = _memoryBytes.putIfAbsent(worker, mm);
	                           mm = tmp == null ? mm : tmp; 
	                         }
	                         mm.update(((Number)val).longValue());
	                     }
	                  }
	              }
	          }
	      };
		  
	      metricServer.serve();
	      String url = metricServer.getUrl();
//	      
	      
	      Cluster cluster = new Cluster(conf);
	      conf.setNumWorkers(parallelism);
	      conf.registerMetricsConsumer(org.apache.storm.metric.LoggingMetricsConsumer.class);
	      conf.registerMetricsConsumer(org.apache.storm.metric.HttpForwardingMetricsConsumer.class, url, 1);
	      Map<String, String> workerMetrics = new HashMap<String, String>();
	      if (!cluster.isLocal()) {
	        //sigar uses JNI and does not work in local mode
	        workerMetrics.put("CPU", "org.apache.storm.metrics.sigar.CPUMetric");
	      }
	      conf.put(Config.TOPOLOGY_WORKER_METRICS, workerMetrics);
	      conf.put(Config.TOPOLOGY_BUILTIN_METRICS_BUCKET_SIZE_SECS, 10);
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
		      StormRunner.runTopologyLocally(tt.builder.createTopology(), topologyName, tt.topologyConfig, tt.runtimeInSeconds);
		    }
		    else {
		      LOG.info("Running in remote (cluster) mode");
		      StormRunner.runTopologyRemotely(tt.builder.createTopology(), topologyName, tt.topologyConfig);
		    }
		    
		
		       
//		     for (int i = 0; i < numMins * 2; i++) {
//		            Thread.sleep(30 * 1000);
//		            writeFile("trying to write at "+ i+ " times");
//		            printMetrics(cluster, topologyName);
//		        }
		    
	
		  }    
		    


	  private static class MemMeasure {
	        private long _mem = 0;
	        private long _time = 0;

	        public synchronized void update(long mem) {
	            _mem = mem;
	            _time = System.currentTimeMillis();
	        }

	        public synchronized long get() {
	            return isExpired() ? 0l : _mem;
	        }

	        public synchronized boolean isExpired() {
	            return (System.currentTimeMillis() - _time) >= 20000;
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
	  
	  
	  private static final Histogram _histo = new Histogram(3600000000000L, 3);
	  private static final AtomicLong _systemCPU = new AtomicLong(0);
	  private static final AtomicLong _userCPU = new AtomicLong(0);
	  private static final AtomicLong _gcCount = new AtomicLong(0);
	  private static final AtomicLong _gcMs = new AtomicLong(0);
	  private static final ConcurrentHashMap<String, MemMeasure> _memoryBytes = new ConcurrentHashMap<String, MemMeasure>();
	   
	  private static long _prev_acked = 0;
	  private static long _prev_uptime = 0;
	  

	  private static long readMemory() {
	    long total = 0;
	    for (MemMeasure mem: _memoryBytes.values()) {
	      total += mem.get();
	    }
	    return total;
	  }

	  public static class Cluster {
		    LocalCluster _local = null;
		    Nimbus.Client _client = null;

		    public Cluster(Map conf) {
		      Map clusterConf = Utils.readStormConfig();
		      if (conf != null) {
		        clusterConf.putAll(conf);
		      }
//		      Boolean isLocal = (Boolean)clusterConf.get("run.local");
		     Boolean isLocal = true;
		      if (isLocal != null && isLocal) {
		        _local = new LocalCluster();
		      } else {
		        _client = NimbusClient.getConfiguredClient(clusterConf).getClient();
		      }
		    }

		    public ClusterSummary getClusterInfo() throws Exception {
		      if (_local != null) {
		        return _local.getClusterInfo();
		      } else {
		        return _client.getClusterInfo();
		      }
		    }

		    public TopologyInfo getTopologyInfo(String id) throws Exception {
		      if (_local != null) {
		        return _local.getTopologyInfo(id);
		      } else {
		        return _client.getTopologyInfo(id);
		      }
		    }

		    public void killTopologyWithOpts(String name, KillOptions opts) throws Exception {
		      if (_local != null) {
		        _local.killTopologyWithOpts(name, opts);
		      } else {
		        _client.killTopologyWithOpts(name, opts);
		      }
		    }

		    public void submitTopology(String name, Map stormConf, StormTopology topology) throws Exception {
		      if (_local != null) {
		        _local.submitTopology(name, stormConf, topology);
		      } else {
		        StormSubmitter.submitTopology(name, stormConf, topology);
		      }
		    }

		    public boolean isLocal() {
		      return _local != null;
		    }
		  }
	  
	  public static void printMetrics(Cluster client, String name) throws Exception {
		    ClusterSummary summary = client.getClusterInfo();
		    String id = null;
		    for (TopologySummary ts: summary.get_topologies()) {
		      if (name.equals(ts.get_name())) {
		        id = ts.get_id();
		      }
		    }
		    if (id == null) {
		      throw new Exception("Could not find a topology named "+name);
		    }
		    TopologyInfo info = client.getTopologyInfo(id);
		    int uptime = info.get_uptime_secs();
		    long acked = 0;
		    long failed = 0;
		    for (ExecutorSummary exec: info.get_executors()) {
		      if ("spout".equals(exec.get_component_id()) && exec.get_stats() != null && exec.get_stats().get_specific() != null) {
		        SpoutStats stats = exec.get_stats().get_specific().get_spout();
		        Map<String, Long> failedMap = stats.get_failed().get(":all-time");
		        Map<String, Long> ackedMap = stats.get_acked().get(":all-time");
		        if (ackedMap != null) {
		          for (String key: ackedMap.keySet()) {
		            if (failedMap != null) {
		              Long tmp = failedMap.get(key);
		              if (tmp != null) {
		                  failed += tmp;
		              }
		            }
		            long ackVal = ackedMap.get(key);
		            acked += ackVal;
		          }
		        }
		      }
		    }
		    long ackedThisTime = acked - _prev_acked;
		    long thisTime = uptime - _prev_uptime;
		    long nnpct, nnnpct, min, max;
		    double mean, stddev;
		    synchronized(_histo) {
		      nnpct = _histo.getValueAtPercentile(99.0);
		      nnnpct = _histo.getValueAtPercentile(99.9);
		      min = _histo.getMinValue();
		      max = _histo.getMaxValue();
		      mean = _histo.getMean();
		      stddev = _histo.getStdDeviation();
		      _histo.reset();
		    }
		    long user = _userCPU.getAndSet(0);
		    long sys = _systemCPU.getAndSet(0);
		    long gc = _gcMs.getAndSet(0);
		    double memMB = readMemory() / (1024.0 * 1024.0);
		    String temp = "uptime: "+uptime + " acked: "+ ackedThisTime+ "acked/sec: "+ (((double)ackedThisTime)/thisTime) + " failed: " + failed+
		    		" min: "+ min +" max: "+ max+ " mean: " + max+" mean"+ mean+
                    "stddev: "+ stddev+ " user: "+user+ " sys "+ sys+ " gc: "+gc+ " mem: "+memMB;
           System.out.println("output message testing "+ temp);
		//      writeFile(temp);    
//		    System.out.printf("uptime: %,4d acked: %,9d acked/sec: %,10.2f failed: %,8d " +
//		                      "99%%: %,15d 99.9%%: %,15d min: %,15d max: %,15d mean: %,15.2f " +
//		                      "stddev: %,15.2f user: %,10d sys: %,10d gc: %,10d mem: %,10.2f\n",
//		                       uptime, ackedThisTime, (((double)ackedThisTime)/thisTime), failed, nnpct, nnnpct,
//		                       min, max, mean, stddev, user, sys, gc, memMB);
		    _prev_uptime = uptime;
		    _prev_acked = acked;
		  }
//	  public static void writeFile(String s){
//		  try{
//			  File file = new File("/home/ubuntu/metrics.txt");
//			 // File file = new File("/Users/yidwa/Desktop/metrics.txt");
//			  if(!file.exists())
//				  file.createNewFile();
//			  FileWriter fw = new FileWriter(file,true);
//			  BufferedWriter bw = new BufferedWriter(fw);
//			  bw.write(s);
//			  bw.flush();
//			  bw.close();
//		  }
//		  catch(IOException e){
//			 e.printStackTrace();
//		  }
//	  }
	  public static void kill(Cluster client, String name) throws Exception {
		    KillOptions opts = new KillOptions();
		    opts.set_wait_secs(0);
		    client.killTopologyWithOpts(name, opts);
		  }
	  
	
}