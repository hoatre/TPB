package storm.tpb.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.json.simple.JSONValue;
import redis.clients.jedis.Jedis;
import storm.kafka.BrokerHosts;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.trident.OpaqueTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.tpb.bolts.RankingsBolt;
import storm.tpb.testing.SlidingWindow;
import storm.tpb.testing.StoreTransactionToMongoDB;
import storm.tpb.bolts.ValueChartBolt;
import storm.tpb.tools.function;
import storm.tpb.util.Properties;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFilter;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.operation.builtin.Count;
import storm.trident.tuple.TridentTuple;

import java.util.List;
import java.util.Map;

/**
 * Created by quangnb on 1/22/15.
 */
public class TopologyControl_new {

    private static Fields valueChartNew = new Fields("listTotal");

    private static final String KAFKA_TOPIC =
            Properties.getString("storm.kafka_topic");

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        //conf.setDebug(true);
        if (args.length == 0) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("log-analysis", conf,
                    createTopology_test());
        } else {
            int workers = Properties.getInt("storm.workers");
            conf.setNumWorkers(workers);
            StormSubmitter.submitTopology(args[0], conf,
                    createTopology());
        }
    }

    public static StormTopology createTopology_test() {

            TridentTopology topology = new TridentTopology();
            BrokerHosts zk = new ZkHosts(Properties.getString("storm.zkhosts"));

            TridentKafkaConfig spoutConf = new TridentKafkaConfig(zk, KAFKA_TOPIC);
            spoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
            spoutConf.startOffsetTime = -1;
            OpaqueTridentKafkaSpout spout = new OpaqueTridentKafkaSpout(spoutConf);

            Stream spoutStream = topology.newStream("kafka-stream", spout);

            Fields jsonFields = new Fields("trx_id", "trx_code", "ch_id", "amount", "acc_no", "prd_id", "timestamp");
            Stream parsedStream = spoutStream
                    .each(new Fields("str"), new
                            JsonProjectFunction(jsonFields), jsonFields)
//                    .each(jsonFields, new JsonProjectFunction_test_filter(jsonFields))
//                    .each(jsonFields, new
//                            JsonProjectFunction_test(jsonFields), new Fields("count"))
//                    .each(jsonFields, new
//                            JsonProjectFunction_test_count(jsonFields), new Fields("asdas"))
                    .aggregate(new Count(), new Fields("count"))
                    .each(new Fields("count"), new
                            JsonProjectFunction_test_count(jsonFields), new Fields("print"));

        return topology.build();

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

        List<String> ChannelCode = function.GetListMongo(Properties.getString("MongoDB.Channel"), "ChannelCode");

        TopologySliding(parsedStream, PARAM.SlidingTime.Time1.getTime(), ChannelCode);
        TopologySliding(parsedStream, PARAM.SlidingTime.Time2.getTime(), ChannelCode);
        TopologySliding(parsedStream, PARAM.SlidingTime.Time3.getTime(), ChannelCode);

        //luu transaction vao DB
        spoutStream.each(new Fields("str"), new StoreTransactionToMongoDB(), new Fields("StoreTransactionToMongoDB"));

        return topology.build();
    }

    private static void TopologySliding(Stream parsedStream, double SlidingTime, List<String> ChannelCode)
    {
        SlidingWindow Sliding = new SlidingWindow().sliding(SlidingTime, SlidingWindow.Time.SECONDS);

        //dem count sum theo channel
        parsedStream.each(new Fields("ch_id", "timestamp", "amount"), new ValueChartBolt(Sliding, SlidingWindow.Time.SECONDS)
                , valueChartNew);

        //Ranking TransactionType
        parsedStream.each(new Fields("amount", "acc_no", "timestamp", "trx_code"), new RankingsBolt(Sliding, SlidingWindow.Time.SECONDS, Integer.parseInt(Properties.getString("Ranking.TOP"))), new Fields("DoneRanking"));
    }

    //add du lieu dau vao thanh JSON
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
            //System.out.println("1.JsonProjectFunction : " + values.toString());
            collector.emit(values);

        }
    }
    //add du lieu dau vao thanh JSON
    public static class JsonProjectFunction_test extends BaseFunction {
        private Fields fields;
        private int count_ia = 0;
        private int partitionIndex;

        public JsonProjectFunction_test(Fields fields) {
            this.fields = fields;
        }

        public void prepare(Map conf, TridentOperationContext context) {
            this.partitionIndex = context.getPartitionIndex();
            int a = context.numPartitions();
        }
        public void execute(TridentTuple tuple, TridentCollector
                collector) {
            try {
                count_ia++;
                System.out.println("I am partition [" + partitionIndex + "]" + "count : " + count_ia + "tuple value : " + tuple.getStringByField("ch_id"));
                //collector.emit(new Values(count_ia));
                Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
                jedis.connect();
                jedis.rpush("test-save-cache", tuple.get(0).toString());
                System.out.println(jedis.llen("test-save-cache"));
                jedis.disconnect();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static class JsonProjectFunction_test_count extends BaseFunction {
        private Fields fields;
        //private int count = 0;
        public JsonProjectFunction_test_count(Fields fields) {
            this.fields = fields;
        }
        public void execute(TridentTuple tuple, TridentCollector
                collector) {
            try{
//                Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
//                jedis.connect();
//                if(jedis.exists("test-count")) {
//                    int count = Integer.parseInt(jedis.get("test-count")) + 1;
//                    jedis.set("test-count", "" + count);
//                }
//                else{
//                    jedis.set("test-count", "1");
//                }
//                jedis.disconnect();
                System.out.println("count end : " + tuple.get(0).toString());
                Thread.sleep(2000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static class JsonProjectFunction_test_filter extends BaseFilter {
        private Fields fields;
        //private int count = 0;
        public JsonProjectFunction_test_filter(Fields fields) {
            this.fields = fields;
        }
        public boolean isKeep(TridentTuple tuple) {
            try{

            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }
    }

}
