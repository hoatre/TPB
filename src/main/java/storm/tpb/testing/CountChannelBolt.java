package storm.tpb.testing;

import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

/**
 * get list sliding
 */
public class CountChannelBolt extends BaseFilter{
    String channel;
    public CountChannelBolt(String channel){
        this.channel = channel;
    }
    public boolean isKeep(TridentTuple tuple) {
        try {
            if(tuple.getStringByField("ch_id").equals(this.channel))
                return true;
            else
                return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
