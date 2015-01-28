package storm.tpb.testing;

import backtype.storm.tuple.Values;

import redis.clients.jedis.Jedis;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.Map;

/**
 * Created by quangnb on 1/20/15.
 */
public class TotalCountAmountBolt extends BaseFunction{
    private SlidingWindow sliding;
    private SlidingWindow.Time emitRatePer;
    private Jedis jedis;
    public TotalCountAmountBolt(SlidingWindow ewma, SlidingWindow.Time emitRatePer){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        this.sliding.mark(tuple.getLongByField("amount"), tuple.getLongByField("timestamp"));
        collector.emit(new Values(this.sliding.getCount(),this.sliding.getSum(),this.sliding.getWindow()));
        System.out.println("count : " + Long.toString(this.sliding.getCount()) + " sum : " + Long.toString(this.sliding.getSum()));
    }
}
