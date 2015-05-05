package storm.tpb.spouts;

/**
 * Created by phonghh on 4/6/15.
 */

import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import storm.tpb.util.Properties;
import storm.trident.operation.TridentCollector;
import storm.trident.spout.IBatchSpout;

import java.util.List;
import java.util.Map;

//@SuppressWarnings({"serial", "rawtypes"})
public class RedisBatch_2_1_Spout implements IBatchSpout {

    private String host;
    private int port;
    private long SlidingTime;
    private long SlidingTimeWait = 0;

    public RedisBatch_2_1_Spout(String host, int port, long SlidingTime) {
        this.host = host;
        this.port = port;
        this.SlidingTime = SlidingTime;
    }

//    public RedisBatchSpout(String host, int port, long SlidingTime, long SlidingTimeWait) {
//        this.host = host;
//        this.port = port;
//        this.SlidingTime = SlidingTime;
//        this.SlidingTimeWait = SlidingTimeWait;
//    }

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
            List<String> list = jedis.lrange("Sliding-data-" + this.SlidingTime, 0, jedis.llen("Sliding-data-" + this.SlidingTime));
            jedis.disconnect();
            collector.emit(new Values(list));
            System.out.println("spout done : " + this.SlidingTime);
//            if(this.SlidingTimeWait == 0)
//                Thread.sleep(Properties.getInt("Load.Interval.Time"));
//            else
//                Thread.sleep(SlidingTimeWait);
            //Thread.yield();
        }catch (Exception e){
            //Thread.yield();
            e.printStackTrace();
        }
    }
}