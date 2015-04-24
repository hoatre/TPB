package storm.tpb.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import storm.tpb.aggregate.*;
import storm.tpb.bolts.*;
import storm.tpb.spouts.RedisBatchSpout;
import storm.tpb.tools.function;
import storm.tpb.util.Properties;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.Sum;

import java.util.ArrayList;
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
            cluster.submitTopology("Topo-2_0", conf,
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


        List<String> TransactionCode = function.GetListMongo(Properties.getString("MongoDB.TransactionTypes"), "TransactionCode");

        TopologySlidingCountSum(PARAM.SlidingTime.Time1.getTime() * 1000, topology, "ch_id");
        TopologySlidingCountSum(PARAM.SlidingTime.Time2.getTime() * 1000, topology, "ch_id");
        TopologySlidingCountSum(PARAM.SlidingTime.Time3.getTime() * 1000, topology, "ch_id");

        TopologySlidingOtherChart(PARAM.SlidingTime.Time1.getTime() * 1000, topology);
        TopologySlidingOtherChart(PARAM.SlidingTime.Time2.getTime() * 1000, topology);
        TopologySlidingOtherChart(PARAM.SlidingTime.Time3.getTime() * 1000, topology);

        TopologySlidingRanking(PARAM.SlidingTime.Time1.getTime() * 1000, topology, TransactionCode);
        TopologySlidingRanking(PARAM.SlidingTime.Time2.getTime() * 1000, topology, TransactionCode);
        TopologySlidingRanking(PARAM.SlidingTime.Time3.getTime() * 1000, topology, TransactionCode);

        TopologySlidingCountChannelByTransaction(PARAM.SlidingTime.Time1.getTime() * 1000, topology);
        TopologySlidingCountChannelByTransaction(PARAM.SlidingTime.Time2.getTime() * 1000, topology);
        TopologySlidingCountChannelByTransaction(PARAM.SlidingTime.Time3.getTime() * 1000, topology);

        TopologySlidingSumAmountTransactionByChannel(PARAM.SlidingTime.Time1.getTime() * 1000, topology);
        TopologySlidingSumAmountTransactionByChannel(PARAM.SlidingTime.Time2.getTime() * 1000, topology);
        TopologySlidingSumAmountTransactionByChannel(PARAM.SlidingTime.Time3.getTime() * 1000, topology);

        return topology.build();
    }

    private static void TopologySlidingCountSum(double slidingTime, TridentTopology topology, String group)
    {
        RedisBatchSpout spout = new RedisBatchSpout(Properties.getString("redis.host"), Properties.getInt("redis.port"), (long)slidingTime);
        Stream spoutStream = topology
                .newStream("stream-" + (long) slidingTime + "CountSum", spout);

        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new SplitChannelBolt(jsonFields), jsonFields)
                .each(jsonFields, new SlidingFilterBolt((long)slidingTime));

        // count tran with each channel
        Stream CountStream = PreStream
                .groupBy(new Fields(group))
                .aggregate(new Fields(group), new Count(), new Fields("count"));

        // sum amount with each channel
        Stream SumStream = PreStream
                .groupBy(new Fields(group))
                .aggregate(new Fields("amount"), new Sum(), new Fields("sum"));

        // merge stream to save redis
        Stream MergeStream = topology
                .join(CountStream, new Fields(group), SumStream, new Fields(group), new Fields(group, "count", "sum"))
                .aggregate(new Fields(group, "count", "sum"), new AddStringCountSum(), new Fields("string"))
                .each(new Fields("string"), new SaveCountSumBolt(slidingTime), new Fields("print"));
    }

    private static void TopologySlidingCountChannelByTransaction(double slidingTime, TridentTopology topology)
    {
        RedisBatchSpout spout = new RedisBatchSpout(Properties.getString("redis.host"), Properties.getInt("redis.port"), (long)slidingTime);
        Stream spoutStream = topology
                .newStream("stream-" + (long) slidingTime + "CountByTran", spout);

        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new SplitChannelBolt(jsonFields), jsonFields)
                .each(jsonFields, new SlidingFilterBolt((long) slidingTime));

        // count tran with each channel
        Stream CountStream = PreStream
                .groupBy(new Fields("ch_id", "trx_code"))
                .aggregate(new Fields("ch_id", "trx_code"), new Count(), new Fields("count"))
                .aggregate(new Fields("ch_id", "trx_code", "count"), new AddStringCountChannelByTran(), new Fields("string"))
                .each(new Fields("string"), new SaveCountChannelByTranBolt(slidingTime), new Fields("print"));
    }

    private static void TopologySlidingSumAmountTransactionByChannel(double slidingTime, TridentTopology topology)
    {
        RedisBatchSpout spout = new RedisBatchSpout(Properties.getString("redis.host"), Properties.getInt("redis.port"), (long)slidingTime);
        Stream spoutStream = topology
                .newStream("stream-" + (long) slidingTime + "SumByTran", spout);

        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new SplitChannelBolt(jsonFields), jsonFields)
                .each(jsonFields, new SlidingFilterBolt((long) slidingTime));

        List<String> fields = new ArrayList<String>();
        fields.add("ch_id");
        fields.add("trx_code");
        fields.add("sum");
        // count tran with each channel
        Stream CountStream = PreStream
                .groupBy(new Fields("ch_id", "trx_code"))
                .aggregate(new Fields("amount"), new Sum(), new Fields("sum"))
                .aggregate(new Fields("ch_id", "trx_code", "sum"), new JoinToListJSONObject(fields), new Fields("string"))
                .each(new Fields("string"), new SaveSumAmountTranByChannelBolt(slidingTime), new Fields("print"));
    }

    private static void TopologySlidingSumAmountByTransaction(double slidingTime, TridentTopology topology)
    {
        RedisBatchSpout spout = new RedisBatchSpout(Properties.getString("redis.host"), Properties.getInt("redis.port"), (long)slidingTime);
        Stream spoutStream = topology
                .newStream("stream-" + (long) slidingTime + "CountByTran", spout);

        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new SplitChannelBolt(jsonFields), jsonFields)
                .each(jsonFields, new SlidingFilterBolt((long) slidingTime));

//        // count tran with each channel
//        Stream CountStream = PreStream
//                .groupBy(new Fields("trx_code"))
//                .aggregate(new Fields("amount"), new Sum(), new Fields("sum"))
//                .aggregate(new Fields("trx_code", "sum"), new JoinToListJSONObject(new ArrayList<String>("trx_code", "sum")), new Fields("string"))
//                .each(new Fields("string"), new SaveCountChannelByTranBolt(slidingTime), new Fields("print"));
    }

    private static void TopologySlidingRanking(double slidingTime, TridentTopology topology, List<String> TransactionCode)
    {
        RedisBatchSpout spout = new RedisBatchSpout(Properties.getString("redis.host"), Properties.getInt("redis.port"), (long)slidingTime);
        Stream spoutStream = topology
                .newStream("stream-" + (long)slidingTime + "Ranking", spout);

        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new SplitChannelBolt(jsonFields), jsonFields)
                .each(jsonFields, new SlidingFilterBolt((long) slidingTime));

        // ranking theo transactionType
        Stream RankingStream = PreStream
                .groupBy(new Fields("trx_code", "acc_no"))
                .aggregate(new Fields("amount"), new Sum(), new Fields("sum"))
                .groupBy(new Fields("trx_code"))
                .aggregate(new Fields("sum", "trx_code", "acc_no"), new AddStringTopBot(), new Fields("string"))
                .each(new Fields("string"), new SaveRankingBolt(Properties.getInt("Ranking.TOP"), slidingTime), new Fields("ranking"));

        // ranking theo non transactionType
        Stream NonRankingStream = PreStream
                .aggregate(new Fields("trx_code"), new AddStringNonRanking(), new Fields("string"))
                .each(new Fields("string"), new SaveNonRankingBolt(TransactionCode, slidingTime), new Fields("listCut"));

    }

    private static void TopologySlidingOtherChart(double slidingTime, TridentTopology topology)
    {
        RedisBatchSpout spout = new RedisBatchSpout(Properties.getString("redis.host"), Properties.getInt("redis.port"), (long)slidingTime);
        Stream spoutStream = topology
                .newStream("stream-" + (long)slidingTime + "OtherChart", spout);

        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new SplitChannelBolt(jsonFields), jsonFields)
                .each(jsonFields, new SlidingFilterBolt((long) slidingTime));

        // count tran with each channel
        Stream CountStream = PreStream
                .groupBy(new Fields("ch_id"))
                .aggregate(new Fields("ch_id"), new Count(), new Fields("count"));

        // sum amount with each channel
        Stream SumStream = PreStream
                .groupBy(new Fields("ch_id"))
                .aggregate(new Fields("amount"), new Sum(), new Fields("sum"));

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