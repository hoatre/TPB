package storm.tpb.testing;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import redis.clients.jedis.Jedis;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.Map;

/**
 * Created by quangnb on 1/20/15.
 */
public class TotalBolt extends BaseRichBolt {
    private OutputCollector collector;
    private SlidingWindow sliding;
    private SlidingWindow.Time emitRatePer;
    private Jedis jedis;
    public TotalBolt(SlidingWindow ewma, SlidingWindow.Time emitRatePer){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
    }
    public void execute(Tuple tuple) {
        //this.sliding.mark(tuple.getIntegerByField("amount"), tuple.getLongByField("timestamp"));
        //collector.emit(new Values(this.sliding.getCount(),this.sliding.getSum()));
       // System.out.println("count : " + Long.toString(this.sliding.getCount()) + " sum : " + Long.toString(this.sliding.getSum()));
    }
    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
    }
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("amount", "timestamp"));
    }
}
