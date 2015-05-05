package storm.tpb.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import storm.tpb.aggregate.*;
import storm.tpb.bolts.*;
import storm.tpb.spouts.RedisBatchSpout;
import storm.tpb.bolts.SaveCountChannelByTranBolt;
import storm.tpb.spouts.RedisBatch_2_1_Spout;
import storm.tpb.tools.function;
import storm.tpb.util.Properties;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.Sum;

import java.util.List;

/**
 * Created by quangnb on 1/22/15.
 */
public class Topology_2_1 {

    private static Fields valueChartNew = new Fields("listTotal");

    private static final String KAFKA_TOPIC =
            Properties.getString("storm.kafka_topic");
    private static Fields jsonFields = new Fields("trx_id", "trx_code","ch_id","amount","acc_no","prd_id","timestamp");
    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        if (args.length == 0) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("Topo-2_1", conf,
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

        TopologySlidingCountSum(PARAM.Time.MINUTES.getTime(), topology, PARAM.Time.SECONDS.getTime());
        TopologySlidingCountSum(PARAM.Time.HOURS.getTime(), topology, PARAM.Time.MINUTES.getTime());
        TopologySlidingCountSum(PARAM.Time.DAYS.getTime(), topology, PARAM.Time.MINUTES.getTime() * 5);

//        TopologySlidingOtherChart(PARAM.SlidingTime.Time1.getTime() * 1000, topology);
//        TopologySlidingOtherChart(PARAM.SlidingTime.Time2.getTime() * 1000, topology);
//        TopologySlidingOtherChart(PARAM.SlidingTime.Time3.getTime() * 1000, topology);
//
//        TopologySlidingRanking(PARAM.SlidingTime.Time1.getTime() * 1000, topology, TransactionCode);
//        TopologySlidingRanking(PARAM.SlidingTime.Time2.getTime() * 1000, topology, TransactionCode);
//        TopologySlidingRanking(PARAM.SlidingTime.Time3.getTime() * 1000, topology, TransactionCode);
//
//        TopologySlidingCountByTransaction(PARAM.SlidingTime.Time1.getTime() * 1000, topology);
//        TopologySlidingCountByTransaction(PARAM.SlidingTime.Time2.getTime() * 1000, topology);
//        TopologySlidingCountByTransaction(PARAM.SlidingTime.Time3.getTime() * 1000, topology);

        return topology.build();
    }

    private static void TopologySlidingCountSum(long slidingTime, TridentTopology topology, long slidingTimeWait)
    {
        RedisBatch_2_1_Spout spout = new RedisBatch_2_1_Spout(Properties.getString("redis.host"), Properties.getInt("redis.port"), slidingTimeWait);
        Stream spoutStream = topology
                .newStream("stream-" + slidingTime + "CountSum_2", spout);

        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new SplitChannel_2_1_Bolt(jsonFields), jsonFields)
                .each(jsonFields, new SlidingFilterBolt(slidingTimeWait));

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
                .each(new Fields("string"), new SaveCountSum_2_1_Bolt(slidingTime, slidingTimeWait), new Fields("print"));
    }

    private static void TopologySlidingCountByTransaction(double slidingTime, TridentTopology topology)
    {
        RedisBatchSpout spout = new RedisBatchSpout(Properties.getString("redis.host"), Properties.getInt("redis.port"), (long)slidingTime);
        Stream spoutStream = topology
                .newStream("stream-" + (long)slidingTime + "CountByTran", spout);

        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new SplitChannelBolt(jsonFields), jsonFields);

        // count tran with each channel
        Stream CountStream = PreStream
                .groupBy(new Fields("ch_id", "trx_code"))
                .aggregate(new Fields("ch_id", "trx_code"), new Count(), new Fields("count"))
                .aggregate(new Fields("ch_id", "trx_code", "count"), new AddStringCountChannelByTran(), new Fields("string"))
                .each(new Fields("string"), new SaveCountChannelByTranBolt(slidingTime), new Fields("print"));
    }

    private static void TopologySlidingOtherChart(double slidingTime, TridentTopology topology)
    {
        RedisBatchSpout spout = new RedisBatchSpout(Properties.getString("redis.host"), Properties.getInt("redis.port"), (long)slidingTime);
        Stream spoutStream = topology
                .newStream("stream-" + (long)slidingTime + "OtherChart", spout);

        //Cut & Split data
        Stream PreStream = spoutStream
                .each(new Fields("list"), new SplitChannelBolt(jsonFields), jsonFields);

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