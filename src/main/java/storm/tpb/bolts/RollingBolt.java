package storm.tpb.bolts;

import storm.tpb.topology.PARAM;
import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

/**
 * Created by quangnb on 1/20/15.
 */
public class RollingBolt extends BaseFilter{
    private String roll;
    public RollingBolt(String roll){
        this.roll = roll;
    }
    public boolean isKeep(TridentTuple tuple) {
        if(tuple.getString(0).equals(this.roll) || tuple.getString(0).equals(PARAM.TransCode.TRANTYPEFAKE.getValue()))
            return true;
        else
            return false;
    }
}
