package storm.tpb.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Fields;
import storm.kafka.BrokerHosts;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.trident.OpaqueTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.tpb.bolts.SaveSlidingBolt;
import storm.tpb.util.Properties;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

/**
 * Created by quangnb on 1/22/15.
 */
public class Topology_1_0 {

    private static Fields valueChartNew = new Fields("listTotal");

    private static final String KAFKA_TOPIC =
            Properties.getString("storm.kafka_topic");
    private static Fields jsonFields = new Fields("trx_id", "trx_code","ch_id","amount","acc_no","prd_id","timestamp");
    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        if (args.length == 0) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("log-analysis", conf,
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
        BrokerHosts zk = new ZkHosts(Properties.getString("storm.zkhosts"));

        TridentKafkaConfig spoutConf = new TridentKafkaConfig(zk,KAFKA_TOPIC);
        spoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
        spoutConf.startOffsetTime =-1;
        OpaqueTridentKafkaSpout spout = new OpaqueTridentKafkaSpout(spoutConf);

        Stream spoutStream = topology.newStream("kafka-stream", spout);


//        Stream parsedStream = spoutStream.each(new Fields("str"), new
//                JsonProjectFunction(jsonFields), jsonFields);

        TopologySliding(spoutStream, PARAM.Time.SECONDS.getTime());
        TopologySliding(spoutStream, PARAM.SlidingTime.Time1.getTime() * 1000);
        TopologySliding(spoutStream, PARAM.SlidingTime.Time2.getTime() * 1000);
        TopologySliding(spoutStream, PARAM.SlidingTime.Time3.getTime() * 1000);

        return topology.build();
    }

    private static void TopologySliding(Stream spoutStream, double slidingTime)
    {
        //SlidingWindow Sliding = new SlidingWindow().sliding(SlidingTime, SlidingWindow.Time.SECONDS);

        //save redis for sliding time
        spoutStream
                .each(new Fields("str"), new SaveSlidingBolt(slidingTime), new Fields("tuple"));
//                .shuffle().each(new Fields("tuple"), new RemoveOutOfSliding(Sliding, SlidingWindow.Time.SECONDS)
//                , valueChartNew).parallelismHint(5);
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
            System.out.println(json.toString());
        }
    }
    //save data for Sliding
    public static class SaveDataSliding extends BaseFunction {
        public SaveDataSliding(){
        }
        public void execute(TridentTuple tuple, TridentCollector
                collector) {
        }
    }
}
