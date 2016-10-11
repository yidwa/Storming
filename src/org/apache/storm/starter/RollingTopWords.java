/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.starter;

import org.apache.storm.Config;
import org.apache.storm.testing.TestWordSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.log4j.Logger;
import org.apache.storm.starter.bolt.IntermediateRankingsBolt;
import org.apache.storm.starter.bolt.RollingCountBolt;
import org.apache.storm.starter.bolt.TotalRankingsBolt;
import org.apache.storm.starter.spout.testWord;
import org.apache.storm.starter.util.StormRunner;

/**
 * This topology does a continuous computation of the top N words that the topology has seen in terms of cardinality.
 * The top N computation is done in a completely scalable way, and a similar approach could be used to compute things
 * like trending topics or trending images on Twitter.
 */
public class RollingTopWords {

  private static final Logger LOG = Logger.getLogger(RollingTopWords.class);
  private static final int DEFAULT_RUNTIME_IN_SECONDS = 60;
  private static final int TOP_N = 5;

  private final TopologyBuilder builder;
  private final String topologyName;
  private final Config topologyConfig;
  private final int runtimeInSeconds;

  public RollingTopWords(String topologyName) throws InterruptedException {
    builder = new TopologyBuilder();
    this.topologyName = topologyName;
    topologyConfig = createTopologyConfiguration();
    runtimeInSeconds = DEFAULT_RUNTIME_IN_SECONDS;

    wireTopology();
  }

  private static Config createTopologyConfiguration() {
    Config conf = new Config();
    conf.setNumWorkers(4);
    conf.setDebug(true);
    return conf;
  }

  private void wireTopology() throws InterruptedException {
    String spoutId = "wordGenerator";
    String counterId = "counter";
    String intermediateRankerId = "intermediateRanker";
    String totalRankerId = "finalRanker";
    builder.setSpout(spoutId, new testWord(), 4);
    builder.setBolt(counterId, new RollingCountBolt(9, 3), 4).fieldsGrouping(spoutId, new Fields("word"));
    builder.setBolt(intermediateRankerId, new IntermediateRankingsBolt(TOP_N), 4).fieldsGrouping(counterId, new Fields(
        "obj"));
    builder.setBolt(totalRankerId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerId);
  }

  public void runLocally() throws InterruptedException {
    StormRunner.runTopologyLocally(builder.createTopology(), topologyName, topologyConfig, runtimeInSeconds);
  }

  public void runRemotely() throws Exception {
    StormRunner.runTopologyRemotely(builder.createTopology(), topologyName, topologyConfig);
  }

  public static void main(String[] args) throws Exception {
    String topologyName = "slidingWindowCounts";
    if (args.length >= 1) {
      topologyName = args[0];
    }
    boolean runLocally = true;
    if (args.length >= 2 && args[1].equalsIgnoreCase("remote")) {
      runLocally = false;
    }

    LOG.info("Topology name: " + topologyName);
    RollingTopWords rtw = new RollingTopWords(topologyName);
    if (runLocally) {
      LOG.info("Running in local mode");
      rtw.runLocally();
    }
    else {
      LOG.info("Running in remote (cluster) mode");
      rtw.runRemotely();
    }
   
    
  }
}
