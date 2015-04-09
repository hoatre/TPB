package storm.tpb.testing;

import backtype.storm.tuple.Values;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import scala.util.parsing.combinator.testing.Str;
import storm.tpb.topology.PARAM;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * get list sliding
 */
public class CutDataForSlidingBolt extends BaseFunction{
    private double Sliding;
    public CutDataForSlidingBolt(double Sliding){
        this.Sliding = Sliding;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            ArrayList<String> list = (ArrayList<String>) tuple.getValue(0);
            List<String> listCut = new ArrayList<String>();
            for(int i = list.size() - 1;i >= 0; i--){
                JSONObject jsonObject = new JSONObject(list.get(i));
                if(jsonObject.getLong("timestamp") >= System.currentTimeMillis() - this.Sliding)
                {
                    listCut.add(list.get(i));
                }
            }
            if(!listCut.isEmpty())
                collector.emit(new Values(listCut));
            System.out.println(listCut.size());
        }catch (Exception e){e.printStackTrace();}
    }
}
