package storm.tpb.testing;

import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

/**
 * Created by quangnb on 1/20/15.
 */
public class ValueChartBolt extends BaseFunction{
    private SlidingWindow sliding;
    private SlidingWindow.Time emitRatePer;
    public ValueChartBolt(SlidingWindow ewma, SlidingWindow.Time emitRatePer){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        this.sliding.TotalCountAmount(tuple.getStringByField("ch_id"), tuple.getLongByField("timestamp"), tuple.getLongByField("amount"));
        collector.emit(new Values(this.sliding.getWindow(),this.sliding.getListTotal()));
    }
}
