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
            List<SlidingWindow.TransactionTotal> listTotal = (ArrayList<SlidingWindow.TransactionTotal>)tuple.get(1);

            this.sliding.chartFlot(listTotal);
        }catch (Exception e)
        {

        }
    }
}
