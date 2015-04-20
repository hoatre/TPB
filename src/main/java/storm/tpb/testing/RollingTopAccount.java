package storm.tpb.testing;

import backtype.storm.Config;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import org.apache.log4j.Logger;
import storm.tpb.bolts.RollingChannelSummaryBolt;
import storm.tpb.bolts.SecondsBolt;
import storm.tpb.spouts.TestTransactionSpout;
import storm.tpb.util.StormRunner;

/**
 * Created by HieuLD on 12/25/14.
 */

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


/**
 * This topology does a continuous computation of the top N words that the topology has seen in terms of cardinality.
 * The top N computation is done in a completely scalable way, and a similar approach could be used to compute things
 * like trending topics or trending images on Twitter.
 */
public class RollingTopAccount {

    private static final Logger LOG = Logger.getLogger(RollingTopAccount.class);
    private static final int DEFAULT_RUNTIME_IN_SECONDS = 120;
    private static final int TOP_N = 5;
    public final static String REDIS_HOST = "localhost";
    public final static int REDIS_PORT = 6379;

    private final TopologyBuilder builder;
    private final String topologyName;
    private final Config topologyConfig;
    private final int runtimeInSeconds;

    public RollingTopAccount(String topologyName) throws InterruptedException {
        builder = new TopologyBuilder();
        this.topologyName = topologyName;
        topologyConfig = createTopologyConfiguration();
        runtimeInSeconds = DEFAULT_RUNTIME_IN_SECONDS;

        wireTopology();
    }

    private static Config createTopologyConfiguration() {
        Config conf = new Config();
        conf.put("redis-host", REDIS_HOST);
        conf.put("redis-port", REDIS_PORT);
        conf.setDebug(true);
        return conf;
    }

    private void wireTopology() throws InterruptedException {
        String spoutId = "transactionGenerator";
        String counterSeconds = "counter_seconds";
        String counterMinutes = "counter_minutes";
        String routerId = "router";
        String intermediateRankerId = "intermediateRanker";
        String totalSecondsRankerId = "finalSecondsRanker";
        String totalMinutesRankerId = "finalMinutesRanker";
        builder.setSpout(spoutId, new TestTransactionSpout(), 5);
        //

        builder.setBolt(routerId, new RouterBolt(), 4).fieldsGrouping(spoutId, new Fields("obj_transaction"));
        builder.setBolt(counterSeconds, new RollingChannelSummaryBolt(1, 1), 4).fieldsGrouping(routerId, new Fields("ch_id"));
        builder.setBolt(totalSecondsRankerId, new SecondsBolt(), 4).globalGrouping(counterSeconds);
//        builder.setBolt(counterMinutes, new RollingChannelSummaryBolt(60, 1), 4).fieldsGrouping(routerId, new Fields("ch_id"));
//        builder.setBolt(totalMinutesRankerId, new MinutesBolt(), 4).globalGrouping(counterMinutes);

        //builder.setBolt(intermediateRankerId, new PrinterBolt()).fieldsGrouping(counterId, new Fields("obj"));
        //builder.setBolt(intermediateRankerId, new IntermediateRankingsBolt(TOP_N), 4).fieldsGrouping(counterId, new Fields("obj"));
        //builder.setBolt(totalRankerId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerId);

    }

    public void runLocally() throws InterruptedException {
        StormRunner.runTopologyLocally(builder.createTopology(), topologyName, topologyConfig, runtimeInSeconds);
    }

    public void runRemotely() throws Exception {
        StormRunner.runTopologyRemotely(builder.createTopology(), topologyName, topologyConfig);
    }

    /**
     * Submits (runs) the topology.
     *
     * Usage: "RollingTopWords [topology-name] [local|remote]"
     *
     * By default, the topology is run locally under the name "slidingWindowCounts".
     *
     * Examples:
     *
     * <pre>
     * {@code
     *
     * # Runs in local mode (LocalCluster), with topology name "slidingWindowCounts"
     * $ storm jar storm-starter-jar-with-dependencies.jar storm.starter.RollingTopWords
     *
     * # Runs in local mode (LocalCluster), with topology name "foobar"
     * $ storm jar storm-starter-jar-with-dependencies.jar storm.starter.RollingTopWords foobar
     *
     * # Runs in local mode (LocalCluster), with topology name "foobar"
     * $ storm jar storm-starter-jar-with-dependencies.jar storm.starter.RollingTopWords foobar local
     *
     * # Runs in remote/cluster mode, with topology name "production-topology"
     * $ storm jar storm-starter-jar-with-dependencies.jar storm.starter.RollingTopWords production-topology remote
     * }
     * </pre>
     *
     * @param args First positional argument (optional) is topology name, second positional argument (optional) defines
     *             whether to run the topology locally ("local") or remotely, i.e. on a real cluster ("remote").
     * @throws Exception
     */
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
        RollingTopAccount rtw = new RollingTopAccount(topologyName);
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

