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
import storm.tpb.bolts.IntermediateRankingsBolt;
import storm.tpb.bolts.PrinterBolt;
import storm.tpb.bolts.TotalRankingsBolt;
import storm.tpb.testing.*;
import storm.tpb.util.Properties;

/**
 * Created by HieuLD on 12/29/14.
 */

public class TransactionTopology
{
    private final Logger LOGGER = Logger.getLogger(this.getClass());
    private static final String KAFKA_TOPIC =
            Properties.getString("storm.kafka_topic");
    private static final int TOP_N = 3;

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
                "Transaction-Topology");
        String spoutId = "transactionGenerator";
        String counterSeconds = "counter_seconds";
        String summaryAccountDeposit = "summaryAccountDeposit";
        String summaryAccountWithdrawal = "summaryAccountWithdrawal";
        String counterMinutes = "counter_minutes";
        String routerId = "router";
        String intermediateRankerDepositId = "intermediateRankerDeposit";
        String intermediateRankerWithdrawalId = "intermediateRankerWithdrawal";

        String totalSecondsRankerId = "finalSecondsRanker";
        String totalMinutesRankerId = "finalMinutesRanker";


        String totalRankerDepositId = "totalRankerDeposit";
        String totalRedisRankerDepositBolt = "RedisRankerDepositBolt";

        String totalRankerWithdrawalId = "totalRankerWithdrawal";
        String totalRedisRankerWithdrawalBolt = "RedisRankerWithdrawalBolt";

        String printBolt = "printBolt";

        kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

        TopologyBuilder topology = new TopologyBuilder();

        topology.setSpout(spoutId, new KafkaSpout(kafkaConf), 3);

        topology.setBolt(routerId, new RouterBolt(), 4).noneGrouping(spoutId);
        topology.setBolt(counterSeconds, new RollingChannelSummaryBolt(1, 1), 4).fieldsGrouping(routerId, new Fields("ch_id"));
        topology.setBolt(totalSecondsRankerId, new SecondsBolt(), 4).globalGrouping(counterSeconds);


        //ranking Deposit
        topology.setBolt(summaryAccountDeposit, new RollingAccountSummaryBolt(60, 5, "Deposit"), 4).fieldsGrouping(routerId, new Fields("acc_no"));
        topology.setBolt(intermediateRankerDepositId, new IntermediateRankingsBolt(TOP_N), 4).fieldsGrouping(summaryAccountDeposit, new Fields("obj"));
        topology.setBolt(totalRankerDepositId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerDepositId);
        topology.setBolt(totalRedisRankerDepositBolt, new MinutesBolt(), 4).fieldsGrouping(totalRankerDepositId, new Fields("rankings"));


        //ranking Withdrawal
        topology.setBolt(summaryAccountWithdrawal, new RollingAccountSummaryBolt(60, 5, "Withdrawal"), 4).fieldsGrouping(routerId, new Fields("acc_no"));
        topology.setBolt(intermediateRankerWithdrawalId, new IntermediateRankingsBolt(TOP_N), 4).fieldsGrouping(summaryAccountWithdrawal, new Fields("obj"));
        topology.setBolt(totalRankerWithdrawalId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerWithdrawalId);
        topology.setBolt(totalRedisRankerWithdrawalBolt, new MinutesBolt(), 4).fieldsGrouping(totalRankerWithdrawalId, new Fields("rankings"));

        return topology.createTopology();
    }

    private static Config createConfig(boolean local)
    {
        int workers = Properties.getInt("storm.workers");
        Config conf = new Config();
        conf.put(Config.NIMBUS_HOST, "localhost");
        conf.setNumAckers(0);
        conf.setDebug(true);
        if (local)
            conf.setMaxTaskParallelism(workers);
        else
            conf.setNumWorkers(workers);
        return conf;
    }
}
