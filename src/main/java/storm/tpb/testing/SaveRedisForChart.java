package storm.tpb.testing;

import redis.clients.jedis.Jedis;
import scala.concurrent.pilib;
import storm.tpb.topology.PARAM;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by quangnb on 1/22/15.
 */
public class SaveRedisForChart extends BaseFunction {
    private Jedis jedis;
    private SlidingWindow sliding;
    private SlidingWindow.Time emitRatePer;
    private List<String> ChannelCode;
    public SaveRedisForChart(SlidingWindow ewma, SlidingWindow.Time emitRatePer, List<String> ChannelCode){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
        this.ChannelCode = ChannelCode;
    }
    public synchronized void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            //this.sliding.chartFlot(tuple.getLongByField("countBranch1"), tuple.getLongByField("countBranch2"), tuple.getLongByField("countBranch3"), tuple.getLongByField("countCenter"));
            jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            List<SlidingWindow.TransactionTotal> listTotal = (ArrayList<SlidingWindow.TransactionTotal>)tuple.get(1);
            //List<String> channelCode = this.sliding.getChannelCode();
            this.sliding.chartFlot(listTotal);
            if(!listTotal.isEmpty()) {
                for (SlidingWindow.TransactionTotal a : listTotal) {
                    jedis.set("real-time-count-" + a.getchannel() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(a.getcount()));
                    jedis.set("real-time-sum-" + a.getchannel() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(a.getamount()));
                }
            }else{
                for(String a : this.ChannelCode) {
                    jedis.set("real-time-count-" + a + "-" + Long.toString(tuple.getLongByField("window")), Integer.toString(0));
                    jedis.set("real-time-sum-" + a + "-" + Long.toString(tuple.getLongByField("window")), Integer.toString(0));
                }
            }
//            jedis.set("real-time-count-" + PARAM.Channel.BRANCH1.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countBranch1")));
//            jedis.set("real-time-count-" + PARAM.Channel.BRANCH2.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countBranch2")));
//            jedis.set("real-time-count-" + PARAM.Channel.BRANCH3.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countBranch3")));
//            jedis.set("real-time-count-" + PARAM.Channel.BRANCH4.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countCenter")));
//            jedis.set("real-time-sum-" + PARAM.Channel.BRANCH1.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("sumBranch1")));
//            jedis.set("real-time-sum-" + PARAM.Channel.BRANCH2.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("sumBranch2")));
//            jedis.set("real-time-sum-" + PARAM.Channel.BRANCH3.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("sumBranch3")));
//            jedis.set("real-time-sum-" + PARAM.Channel.BRANCH4.getValue() + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("sumCenter")));
        }catch (Exception e)
        {

        }
    }
}
