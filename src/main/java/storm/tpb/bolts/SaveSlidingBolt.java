package storm.tpb.bolts;

import backtype.storm.tuple.Values;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import storm.tpb.topology.PARAM;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

/**
 * get list sliding
 */
public class SaveSlidingBolt extends BaseFunction{
    private double sliding;
    public SaveSlidingBolt(double sliding){
        this.sliding = sliding;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            JSONObject jsonObject = new JSONObject(tuple.getString(0));

            Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            jedis.connect();
           // if (!jsonObject.getString("ch_id").equals(PARAM.Channel.CHANNELFAKE.getValue())) {
                //this.sliding.listSliding.add(jsonObject.toString());
                jedis.rpush("Sliding-data-" + (long)this.sliding, tuple.getString(0));
            //}
            jedis.disconnect();
            //collector.emit(tuple);
        }catch (Exception e){e.printStackTrace();}
    }
}
