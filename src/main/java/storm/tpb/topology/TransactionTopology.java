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
import storm.tpb.bolts.IntermediateRankingsBotBolt;
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
    private static final int TOP_N = 5;

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
        String summaryAccountDepositBot = "summaryAccountDepositBot";
        String summaryAccountWithdrawal = "summaryAccountWithdrawal";
        String summaryAccountWithdrawalBot = "summaryAccountWithdrawalBot";
        String counterMinutes = "counter_minutes";
        String routerId = "router";
        String intermediateRankerDepositId = "intermediateRankerDeposit";
        String intermediateRankerWithdrawalId = "intermediateRankerWithdrawal";
        String intermediateRankerDepositIdBot = "intermediateRankerDepositBot";
        String intermediateRankerWithdrawalIdBot = "intermediateRankerWithdrawalBot";

        String totalSecondsRankerId = "finalSecondsRanker";
        String totalMinutesRankerId = "finalMinutesRanker";
        String totalSecondsRankerIdBot = "finalSecondsRankerBot";
        String totalMinutesRankerIdBot = "finalMinutesRankerBot";


        String totalRankerDepositId = "totalRankerDeposit";
        String totalRedisRankerDepositBolt = "RedisRankerDepositBolt";
        String totalRankerDepositIdBot = "totalRankerDepositBot";
        String totalRedisRankerDepositBoltBot = "RedisRankerDepositBoltBot";

        String totalRankerWithdrawalId = "totalRankerWithdrawal";
        String totalRedisRankerWithdrawalBolt = "RedisRankerWithdrawalBolt";
        String totalRankerWithdrawalIdBot = "totalRankerWithdrawalBot";
        String totalRedisRankerWithdrawalBoltBot = "RedisRankerWithdrawalBoltBot";

        String summaryAccountTransferFrom = "summaryAccountTransferFrom";
        String intermediateRankerTransferFromId ="intermediateRankerTransferFromId";
        String totalRankerTransferFromId = "totalRankerTransferFromId";
        String totalRedisRankerTransferFromBolt = "totalRedisRankerTransferFromBolt";

        String intermediateRankerTransferFromIdBot ="intermediateRankerTransferFromIdBot";
        String totalRankerTransferFromIdBot ="totalRankerTransferFromIdBot";
        String totalRedisRankerTransferFromBoltBot ="totalRedisRankerTransferFromBoltBot";

                String printBolt = "printBolt";

        kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

        TopologyBuilder topology = new TopologyBuilder();

        topology.setSpout(spoutId, new KafkaSpout(kafkaConf), 99);

        topology.setBolt(routerId, new RouterBolt(), 99).noneGrouping(spoutId);
        topology.setBolt(counterSeconds, new RollingChannelSummaryBolt(1, 1), 99).fieldsGrouping(routerId, new Fields("ch_id"));
        topology.setBolt(totalSecondsRankerId, new SecondsBolt(), 99).globalGrouping(counterSeconds);


        //ranking Deposit
        topology.setBolt(summaryAccountDeposit, new RollingAccountSummaryBolt(30, 5, "Deposit"), 99).fieldsGrouping(routerId, new Fields("acc_no"));
        topology.setBolt(intermediateRankerDepositId, new IntermediateRankingsBolt(TOP_N), 99).fieldsGrouping(summaryAccountDeposit, new Fields("obj"));
        topology.setBolt(totalRankerDepositId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerDepositId);
        topology.setBolt(totalRedisRankerDepositBolt, new MinutesBolt("Depsits"), 99).fieldsGrouping(totalRankerDepositId, new Fields("rankings"));

        //ranking Deposit Bot
        //topology.setBolt(summaryAccountDepositBot, new RollingAccountSummaryBolt(60, 5, "Deposit"), 4).fieldsGrouping(routerId, new Fields("acc_no"));
        topology.setBolt(intermediateRankerDepositIdBot, new IntermediateRankingsBotBolt(TOP_N), 99).fieldsGrouping(summaryAccountDeposit, new Fields("obj"));
        topology.setBolt(totalRankerDepositIdBot, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerDepositIdBot);
        topology.setBolt(totalRedisRankerDepositBoltBot, new MinutesBotBolt("Depsits"), 99).fieldsGrouping(totalRankerDepositIdBot, new Fields("rankings"));

        //ranking Withdrawal
        topology.setBolt(summaryAccountWithdrawal, new RollingAccountSummaryBolt(30, 5, "Withdrawal"), 99).fieldsGrouping(routerId, new Fields("acc_no"));
        topology.setBolt(intermediateRankerWithdrawalId, new IntermediateRankingsBolt(TOP_N), 99).fieldsGrouping(summaryAccountWithdrawal, new Fields("obj"));
        topology.setBolt(totalRankerWithdrawalId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerWithdrawalId);
        topology.setBolt(totalRedisRankerWithdrawalBolt, new MinutesBolt("Withdrawals"), 99).fieldsGrouping(totalRankerWithdrawalId, new Fields("rankings"));

        //ranking Withdrawal Bot
        //topology.setBolt(summaryAccountWithdrawalBot, new RollingAccountSummaryBolt(60, 5, "Withdrawal"), 4).fieldsGrouping(routerId, new Fields("acc_no"));
        topology.setBolt(intermediateRankerWithdrawalIdBot, new IntermediateRankingsBotBolt(TOP_N), 99).fieldsGrouping(summaryAccountWithdrawal, new Fields("obj"));
        topology.setBolt(totalRankerWithdrawalIdBot, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerWithdrawalIdBot);
        topology.setBolt(totalRedisRankerWithdrawalBoltBot, new MinutesBotBolt("Withdrawals"), 99).fieldsGrouping(totalRankerWithdrawalIdBot, new Fields("rankings"));

        //ranking Transfer
        topology.setBolt(summaryAccountTransferFrom, new RollingAccountSummaryBolt(30, 5, "Transfer From"), 99).fieldsGrouping(routerId, new Fields("acc_no"));
        topology.setBolt(intermediateRankerTransferFromId, new IntermediateRankingsBolt(TOP_N), 99).fieldsGrouping(summaryAccountTransferFrom, new Fields("obj"));
        topology.setBolt(totalRankerTransferFromId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerTransferFromId);
        topology.setBolt(totalRedisRankerTransferFromBolt, new MinutesBolt("TransferFrom"), 99).fieldsGrouping(totalRankerTransferFromId, new Fields("rankings"));

        //ranking Transfer Bot
        //topology.setBolt(summaryAccountWithdrawalBot, new RollingAccountSummaryBolt(60, 5, "Withdrawal"), 4).fieldsGrouping(routerId, new Fields("acc_no"));
        topology.setBolt(intermediateRankerTransferFromIdBot, new IntermediateRankingsBotBolt(TOP_N), 99).fieldsGrouping(summaryAccountTransferFrom, new Fields("obj"));
        topology.setBolt(totalRankerTransferFromIdBot, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerTransferFromIdBot);
        topology.setBolt(totalRedisRankerTransferFromBoltBot, new MinutesBotBolt("TransferFrom"), 99).fieldsGrouping(totalRankerTransferFromIdBot, new Fields("rankings"));

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
