package storm.tpb.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.tpb.testing.MinutesBolt;
import storm.tpb.testing.RollingChannelSummaryBolt;
import storm.tpb.testing.RouterBolt;
import storm.tpb.testing.SecondsBolt;
import storm.tpb.util.Properties;

/**
 * Created by HieuLD on 12/29/14.
 */

public class TransactionTopology
{
    private final Logger LOGGER = Logger.getLogger(this.getClass());
    private static final String KAFKA_TOPIC =
            Properties.getString("storm.kafka_topic");

    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();

        if (args != null && args.length > 0)
        {
            StormSubmitter.submitTopology(
                    args[0],
                    createConfig(false),
                    createTopology());
        }
        else
        {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(
                    "Transaction-Topology",
                    createConfig(true),
                    createTopology());
            //Thread.sleep(60000);
            //cluster.shutdown();
        }
        Utils.sleep(10000);
    }

    private static StormTopology createTopology()
    {
        SpoutConfig kafkaConf = new SpoutConfig(
                new ZkHosts(Properties.getString("storm.zkhosts")),
                KAFKA_TOPIC,
                "",
                "TransactionSpout");
        String spoutId = "transactionGenerator";
        String counterSeconds = "counter_seconds";
        String counterMinutes = "counter_minutes";
        String routerId = "router";
        String intermediateRankerId = "intermediateRanker";
        String totalSecondsRankerId = "finalSecondsRanker";
        String totalMinutesRankerId = "finalMinutesRanker";

        kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());
        TopologyBuilder topology = new TopologyBuilder();

        topology.setSpout(spoutId, new KafkaSpout(kafkaConf), 3);

        topology.setBolt(routerId, new RouterBolt(), 4).fieldsGrouping(spoutId, new Fields("obj_transaction"));
        topology.setBolt(counterSeconds, new RollingChannelSummaryBolt(1, 1), 4).fieldsGrouping(routerId, new Fields("ch_id"));
        topology.setBolt(totalSecondsRankerId, new SecondsBolt(), 4).globalGrouping(counterSeconds);
        topology.setBolt(counterMinutes, new RollingChannelSummaryBolt(60, 1), 4).fieldsGrouping(routerId, new Fields("ch_id"));
        topology.setBolt(totalMinutesRankerId, new MinutesBolt(), 4).globalGrouping(counterMinutes);


        return topology.createTopology();
    }

    private static Config createConfig(boolean local)
    {
        int workers = Properties.getInt("storm.workers");
        Config conf = new Config();
        conf.put(Config.NIMBUS_HOST, "localhost");
        conf.setDebug(true);
        if (local)
            conf.setMaxTaskParallelism(workers);
        else
            conf.setNumWorkers(workers);
        return conf;
    }
}
