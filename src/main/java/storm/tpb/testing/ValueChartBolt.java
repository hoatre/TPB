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
    private Jedis jedis;
    public ValueChartBolt(SlidingWindow ewma, SlidingWindow.Time emitRatePer){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        this.sliding.chart(tuple.getStringByField("ch_id"), tuple.getLongByField("timestamp"));
        collector.emit(new Values(this.sliding.getCountBranch1(),this.sliding.getCountBranch2()
                                    ,this.sliding.getCountBranch3(),this.sliding.getCountCenter()));
        System.out.println("getCountBranch1 : " + this.sliding.getCountBranch1() + " getCountBranch2 : " + this.sliding.getCountBranch2()
                + " getCountBranch3 : " + this.sliding.getCountBranch3() + " getCountCenter : " + this.sliding.getCountCenter());
    }
}
