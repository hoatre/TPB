package storm.tpb.testing;

import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import scala.collection.parallel.ParIterableLike;
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
    public RankingsBolt(SlidingWindow ewma, SlidingWindow.Time emitRatePer){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        this.sliding.listAmountAcc(tuple.getStringByField("trx_code"), tuple.getLongByField("amount"),
                tuple.getStringByField("acc_no"), tuple.getLongByField("timestamp"));
    }
}
