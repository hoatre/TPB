package storm.tpb.testing;

import backtype.storm.tuple.Values;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

/**
 * Created by quangnb on 1/20/15.
 */
public class MovingAverageFunction extends BaseFunction {
//    private static final Logger LOG = LoggerFactory.
//            getLogger(BaseFunction.class);
    private SlidingWindow sliding;
    private SlidingWindow.Time emitRatePer;
    public MovingAverageFunction(SlidingWindow ewma, SlidingWindow.Time emitRatePer){
        this.sliding = ewma;
        this.emitRatePer = emitRatePer;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        this.sliding.mark(tuple.getLong(0), tuple.getLong(1));
        //System.out.println("Rate: " + this.sliding.getAverageRatePer(this.emitRatePer));
        collector.emit(new Values(this.sliding.getAverageRatePer(this.emitRatePer)));
    }
}
