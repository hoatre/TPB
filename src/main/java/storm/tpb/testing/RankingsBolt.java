package storm.tpb.testing;

import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import scala.collection.parallel.ParIterableLike;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.Collections;

/**
 * Created by quangnb on 1/20/15.
 */
public class RankingsBolt extends BaseFunction{
    private SlidingWindow sliding;
    private SlidingWindow.Time emitRatePer;
    public RankingsBolt(SlidingWindow ewma, SlidingWindow.Time emitRatePer){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        this.sliding.listAmountAcc(tuple.getLongByField("amount"),
                tuple.getStringByField("acc_no"), tuple.getLongByField("timestamp"));
        System.out.println("TopFive : " + this.sliding.getTopFive());
        System.out.println("BotFive : " + this.sliding.getBotFive());
        collector.emit(new Values(this.sliding.getTopFive(),this.sliding.getBotFive()));
    }
}
