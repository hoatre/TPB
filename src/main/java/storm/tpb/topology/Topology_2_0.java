package storm.tpb.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.Grouping;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import clojure.lang.Numbers;
import storm.kafka.BrokerHosts;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.trident.OpaqueTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.tpb.testing.*;
import storm.tpb.tools.function;
import storm.tpb.util.Properties;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.CombinerAggregator;
import storm.trident.operation.builtin.*;
import storm.trident.operation.builtin.Count;
import storm.trident.testing.FixedBatchSpout;
import storm.trident.tuple.TridentTuple;

import java.util.List;

/**
 * Created by quangnb on 1/22/15.
 */
public class Topology_2_0 {

    private static Fields valueChartNew = new Fields("listTotal");

    private static final String KAFKA_TOPIC =
            Properties.getString("storm.kafka_topic");
    private static Fields jsonFields = new Fields("trx_id", "trx_code","ch_id","amount","acc_no","prd_id","timestamp");
    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        if (args.length == 0) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("Topo-2", conf,
                    createTopology());
        } else {
            int workers = Properties.getInt("storm.workers");
            conf.setNumWorkers(workers);
            StormSubmitter.submitTopology(args[0], conf,
                    createTopology());
        }
    }

    public static StormTopology createTopology() {

        TridentTopology topology = new TridentTopology();
        RedisBatchSpout spout = new RedisBatchSpout(Properties.getString("redis.host"), Properties.getInt("redis.port"));
        Stream spoutStream = topology.newStream("stream", spout);

        List<String> TransactionCode = function.GetListMongo(Properties.getString("MongoDB.TransactionTypes"), "TransactionCode");

        TopologySliding(spoutStream, PARAM.SlidingTime.Time1.getTime() * 1000, topology);
        TopologySliding(spoutStream, PARAM.SlidingTime.Time2.getTime() * 1000, topology);
        TopologySliding(spoutStream, PARAM.SlidingTime.Time3.getTime() * 1000, topology);

        return topology.build();
    }

    private static void TopologySliding(Stream spoutStream, double slidingTime, TridentTopology topology)
    {
        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new CutDataForSlidingBolt(slidingTime), new Fields("listCut"))
                .each(new Fields("listCut"), new SplitChannelBolt(jsonFields), jsonFields);

        // count tran with each channel
        Stream CountStream = PreStream
                .groupBy(new Fields("ch_id"))
                .aggregate(new Fields("ch_id"), new Count(), new Fields("count"));

        // sum amount with each channel
        Stream SumStream = PreStream
                .groupBy(new Fields("ch_id"))
                .aggregate(new Fields("amount"), new Sum(), new Fields("sum"));

        // merge stream to save redis
        Stream MergeStream = topology
                .join(CountStream, new Fields("ch_id"), SumStream, new Fields("ch_id"), new Fields("ch_id", "count", "sum"))
                .aggregate(new Fields("ch_id", "count", "sum"), new AddStringCountSum(), new Fields("string"))
                .each(new Fields("string"), new SaveCountSumBolt(slidingTime), new Fields("print"));

        // ranking theo transactionType
        Stream RankingStream = PreStream
                .groupBy(new Fields("trx_code", "acc_no"))
                .aggregate(new Fields("amount"), new Sum(), new Fields("sum"))
                .groupBy(new Fields("trx_code"))
                .aggregate(new Fields("sum", "trx_code", "acc_no"), new AddStringTopBot(), new Fields("string"))
                .each(new Fields("string"), new SaveRankingBolt(Properties.getInt("Ranking.TOP"),  slidingTime), new Fields("test"));

        // count tran with each transaction
        Stream CountTranStream = PreStream
                .groupBy(new Fields("trx_code"))
                .aggregate(new Fields("trx_code"), new Count(), new Fields("count"))
                .aggregate(new Fields("trx_code", "count"), new AddStringCountTran(), new Fields("string"))
                .each(new Fields("string"), new SaveCountTranBolt(slidingTime), new Fields("doneSave"));

        // count tran with each product
        Stream CountProductStream = PreStream
                .groupBy(new Fields("prd_id"))
                .aggregate(new Fields("prd_id"), new Count(), new Fields("count"))
                .aggregate(new Fields("prd_id", "count"), new AddStringCountProduct(), new Fields("string"))
                .each(new Fields("string"), new SaveCountProductBolt(slidingTime), new Fields("doneSave"));
    }
}