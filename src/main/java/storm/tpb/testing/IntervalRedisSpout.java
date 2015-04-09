package storm.tpb.testing;

/**
 * Created by phonghh on 4/6/15.
 */

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

//@SuppressWarnings({"serial", "rawtypes"})
public class IntervalRedisSpout implements IRichSpout {
    boolean isDistributed;
    SpoutOutputCollector collector;
    private String host;
    private int port;
    private String pattern;

    //public static final String tupple = new String();

//    public IntervalRedisSpout() {
//        this(true);
//    }

    public IntervalRedisSpout(String host, int port) {
        this(true);
        this.host = host;
        this.port = port;
    }

    public IntervalRedisSpout(boolean isDistributed) {
        this.isDistributed = isDistributed;
    }

    public boolean isDistributed() {
        return this.isDistributed;
    }

    @SuppressWarnings("rawtypes")
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    public void close() {

    }

    public void nextTuple() {
        try {
            Jedis jedis = new Jedis(host, port);
            jedis.connect();
            List<String> list = jedis.lrange("Sliding-data", 0, jedis.llen("Sliding-data"));
            jedis.disconnect();
            this.collector.emit(new Values(list));
            Thread.sleep(1000);
            //Thread.yield();

        }catch (Exception e){
            Thread.yield();
            e.printStackTrace();
        }
    }

    public void ack(Object msgId) {

    }

    public void fail(Object msgId) {

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("list"));
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}