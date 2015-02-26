package storm.tpb.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.json.simple.JSONValue;
import storm.kafka.BrokerHosts;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.trident.OpaqueTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.kafka.trident.ZkBrokerReader;
import storm.tpb.testing.*;
import storm.tpb.tools.function;
import storm.tpb.util.Properties;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.builtin.Sum;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by quangnb on 1/22/15.
 */
public class TopologyControl {

    private static Fields totalCountAmount = new Fields("count", "sumAmount", "window");
    private static Fields valueChart = new Fields("countBranch1", "countBranch2", "countBranch3", "countCenter", "window"
                                                    ,"sumBranch1", "sumBranch2", "sumBranch3", "sumCenter");
    private static Fields valueChartNew = new Fields("window", "listTotal");
    private static Fields RankingField = new Fields("TopFiveDep", "BotFiveDep", "TopFiveWit", "BotFiveWit", "TopFiveTran", "BotFiveTran", "window");

    private static final String KAFKA_TOPIC =
            Properties.getString("storm.kafka_topic");

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        if (args.length == 0) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("log-analysis", conf,
                    createTopology());
        } else {
//            int workers = Properties.getInt("storm.workers");
//            conf.setNumWorkers(workers);
            StormSubmitter.submitTopology(args[0], conf,
                    createTopology());
        }
    }

    public static StormTopology createTopology() {

        TridentTopology topology = new TridentTopology();
        BrokerHosts zk = new ZkHosts(Properties.getString("storm.zkhosts"));

        TridentKafkaConfig spoutConf = new TridentKafkaConfig(zk,KAFKA_TOPIC);
        spoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
        spoutConf.startOffsetTime =-1;
        OpaqueTridentKafkaSpout spout = new OpaqueTridentKafkaSpout(spoutConf);

        Stream spoutStream = topology.newStream("kafka-stream",spout);
        Fields jsonFields = new Fields("trx_id", "trx_code","ch_id","amount","acc_no","prd_id","timestamp");
        Stream parsedStream = spoutStream.each(new Fields("str"), new
                JsonProjectFunction(jsonFields), jsonFields);

        List<String> TransactionCode = function.GetListMongo(Properties.getString("MongoDB.TransactionTypes"), "TransactionCode");
        List<String> ChannelCode = function.GetListMongo(Properties.getString("MongoDB.Channel"), "ChannelCode");

        TopologySliding(parsedStream, 60.0, TransactionCode, ChannelCode);
        TopologySliding(parsedStream, 3600.0, TransactionCode, ChannelCode);
        TopologySliding(parsedStream, 86400.0, TransactionCode, ChannelCode);

        spoutStream.each(new Fields("str"), new StoreTransactionToMongoDB(), new Fields("StoreTransactionToMongoDB"));

        return topology.build();
    }

    private  static void TotalRankingByTranType(Stream st, SlidingWindow Sliding, String tranType){
        st
            .each(new Fields("amount", "acc_no", "timestamp", "trx_code"), new RankingsBolt(Sliding, SlidingWindow.Time.SECONDS), RankingField);
    }

    private static void TopologySliding(Stream parsedStream, double SlidingTime, List<String> TransactionCode, List<String> ChannelCode)
    {
        SlidingWindow Sliding = new SlidingWindow().sliding(SlidingTime, SlidingWindow.Time.SECONDS);

        //Count channel for chart
        parsedStream.each(new Fields("ch_id", "timestamp", "amount"), new ValueChartBolt(Sliding, SlidingWindow.Time.SECONDS)
                , valueChartNew)
                .each(valueChartNew, new SaveRedisForChart(Sliding, SlidingWindow.Time.SECONDS, ChannelCode), new Fields("doneValueChart"));

        for(String code : TransactionCode)
            TotalRankingByTranType(parsedStream, Sliding, code);
    }

    public static class JsonProjectFunction extends BaseFunction {
        private Fields fields;
        public JsonProjectFunction(Fields fields) {
            this.fields = fields;
        }
        public void execute(TridentTuple tuple, TridentCollector
                collector) {
            String json = tuple.getString(0);
            Map<String, Object> map = (Map<String, Object>)
                    JSONValue.parse(json);
            Values values = new Values();
            for (int i = 0; i < this.fields.size(); i++) {
                values.add(map.get(this.fields.get(i)));
            }

            collector.emit(values);
        }
    }
}
