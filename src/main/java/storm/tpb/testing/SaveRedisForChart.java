package storm.tpb.testing;

import redis.clients.jedis.Jedis;
import storm.tpb.topology.PARAM;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.net.SocketTimeoutException;

/**
 * Created by quangnb on 1/22/15.
 */
public class SaveRedisForChart extends BaseFunction {
    private Jedis jedis;
    private SlidingWindow sliding;
    private SlidingWindow.Time emitRatePer;
    public SaveRedisForChart(SlidingWindow ewma, SlidingWindow.Time emitRatePer){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
    }
    public synchronized void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            this.sliding.chartFlot(tuple.getLongByField("countBranch1"), tuple.getLongByField("countBranch2"), tuple.getLongByField("countBranch3"), tuple.getLongByField("countCenter"));
            jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            jedis.set("real-time-count-" + PARAM.Channel.BRANCH1.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countBranch1")));
            jedis.set("real-time-count-" + PARAM.Channel.BRANCH2.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countBranch2")));
            jedis.set("real-time-count-" + PARAM.Channel.BRANCH3.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countBranch3")));
            jedis.set("real-time-count-" + PARAM.Channel.BRANCH4.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countCenter")));
            jedis.set("real-time-sum-" + PARAM.Channel.BRANCH1.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("sumBranch1")));
            jedis.set("real-time-sum-" + PARAM.Channel.BRANCH2.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("sumBranch2")));
            jedis.set("real-time-sum-" + PARAM.Channel.BRANCH3.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("sumBranch3")));
            jedis.set("real-time-sum-" + PARAM.Channel.BRANCH4.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("sumCenter")));
        }catch (Exception e)
        {

        }
    }
}
