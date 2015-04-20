package storm.tpb.bolts;

import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import scala.collection.parallel.ParIterableLike;
import storm.tpb.testing.SlidingWindow;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.Collections;

/**
 * tinh ranking acc theo transaction type
 */
public class RankingsBolt extends BaseFunction{
    private SlidingWindow sliding;
    private SlidingWindow.Time emitRatePer;
    private int TOP;
    public RankingsBolt(SlidingWindow ewma, SlidingWindow.Time emitRatePer, int TOP){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
        this.TOP = TOP;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        System.out.println("RankingsBoltBG");
        this.sliding.listAmountAcc(tuple.getStringByField("trx_code"), tuple.getLongByField("amount"),
                tuple.getStringByField("acc_no"), tuple.getLongByField("timestamp"), this.TOP);
        System.out.println("RankingsBolt");
    }
}
