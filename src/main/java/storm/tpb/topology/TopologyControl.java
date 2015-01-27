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
import storm.tpb.testing.*;
import storm.tpb.util.Properties;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.builtin.Sum;
import storm.trident.tuple.TridentTuple;

import java.util.Map;

/**
 * Created by quangnb on 1/22/15.
 */
public class TopologyControl {

    private static final String KAFKA_TOPIC =
            Properties.getString("storm.kafka_topic");

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        //conf.setMaxSpoutPending(500);
        //conf.put("task.heartbeat.frequency.secs",3);
        if (args.length == 0) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("log-analysis", conf,
                    createTopology());
        } else {
            //conf.setNumWorkers(3);
            StormSubmitter.submitTopology(args[0], conf,
                    createTopology());
        }
    }

    public static StormTopology createTopology() {

        Fields valueChart = new Fields("countBranch1", "countBranch2", "countBranch3", "countCenter");
        Fields totalCountAmount = new Fields("count", "sumAmount");

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

        SlidingWindow Sliding30s = new SlidingWindow().sliding(30.0, SlidingWindow.Time.SECONDS);

        //Total Tran & Amount
        parsedStream.each(new Fields("amount", "timestamp"), new TotalCountAmountBolt(Sliding30s, SlidingWindow.Time.SECONDS)
                        , new Fields("count", "sumAmount"))
                    .each(totalCountAmount, new SaveRedisTotalCountAmount(), new Fields("doneCountAmount"));

        //Count channel for chart
        parsedStream.each(new Fields("ch_id", "timestamp"), new ValueChartBolt(Sliding30s, SlidingWindow.Time.SECONDS)
                            , valueChart)
                    .each(valueChart, new SaveRedisForChart(), new Fields("doneValueChart"));

        //Rankings DEPOSIT
        parsedStream.each(new Fields("trx_code"), new RollingBolt(PARAM.TransCode.DEPOSIT.getValue()))
                    .each(new Fields("amount", "acc_no", "timestamp"), new RankingsBolt(Sliding30s, SlidingWindow.Time.SECONDS), new Fields("TopFive", "BotFive"))
                    .each(new Fields("TopFive", "BotFive"), new SaveRedisTopBotBolt(PARAM.TransCode.DEPOSIT.getValue()), new Fields("abc"));

        //Rankings TRANSFERFROM
        parsedStream.each(new Fields("trx_code"), new RollingBolt(PARAM.TransCode.TRANSFERFROM.getValue()))
                    .each(new Fields("amount", "acc_no", "timestamp"), new RankingsBolt(Sliding30s, SlidingWindow.Time.SECONDS), new Fields("TopFive", "BotFive"))
                    .each(new Fields("TopFive", "BotFive"), new SaveRedisTopBotBolt(PARAM.TransCode.TRANSFERFROM.getValue()), new Fields("abc"));

        //Rankings WITHDRAWAL
        parsedStream.each(new Fields("trx_code"), new RollingBolt(PARAM.TransCode.WITHDRAWAL.getValue()))
                    .each(new Fields("amount", "acc_no", "timestamp"), new RankingsBolt(Sliding30s, SlidingWindow.Time.SECONDS), new Fields("TopFive", "BotFive"))
                    .each(new Fields("TopFive", "BotFive"), new SaveRedisTopBotBolt(PARAM.TransCode.WITHDRAWAL.getValue()), new Fields("abc"));


        return topology.build();
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
            System.out.println("All values : " + values);

            collector.emit(values);
        }
    }
}
