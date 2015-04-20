package storm.tpb.bolts;

import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

/**
 * Created by phonghh on 4/20/15.
 */
public class SlidingFilterBolt extends BaseFilter {

    private long SlidingTime;
    public SlidingFilterBolt(long SlidingTime){
        this.SlidingTime = SlidingTime;
    }
    @Override
    public boolean isKeep(TridentTuple tuple) {
        if(tuple.getLongByField("timestamp") > System.currentTimeMillis() - this.SlidingTime)
            return true;
        else
            return false;
    }
}
