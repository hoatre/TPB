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
import storm.tpb.util.Properties;
import storm.trident.operation.TridentCollector;
import storm.trident.spout.IBatchSpout;

import java.util.List;
import java.util.Map;

//@SuppressWarnings({"serial", "rawtypes"})
public class RedisBatchSpout implements IBatchSpout {

    private String host;
    private int port;

    public RedisBatchSpout(String host, int port) {
        this.host = host;
        this.port = port;
    }
    @Override
    public void close() {

    }

    @Override
    public Fields getOutputFields() {
        return new Fields("list");
    }

    @Override
    public Map getComponentConfiguration() {
        return null;
    }

    @Override
    public void open(Map map, TopologyContext topologyContext) {

    }

    @Override
    public void ack(long l) {

    }

    @Override
    public void emitBatch(long l, TridentCollector collector) {
        try {
            Jedis jedis = new Jedis(host, port);
            jedis.connect();
            List<String> list = jedis.lrange("Sliding-data", 0, jedis.llen("Sliding-data"));
            jedis.disconnect();
            collector.emit(new Values(list));
            System.out.println("spout done");
            Thread.sleep(Properties.getInt("Load.Interval.Time"));
            Thread.yield();
        }catch (Exception e){
            Thread.yield();
            e.printStackTrace();
        }
    }
}