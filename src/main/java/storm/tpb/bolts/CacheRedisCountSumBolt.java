package storm.tpb.bolts;

import backtype.storm.tuple.Values;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * get list sliding
 */
public class CacheRedisCountSumBolt extends BaseFunction{
    private double Sliding;
    List<String> ChannelCode;
    public CacheRedisCountSumBolt(double Sliding, List<String> ChannelCode){
        this.Sliding = Sliding;
        this.ChannelCode = ChannelCode;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            jedis.connect();

            for(String channel : this.ChannelCode){
                JSONObject jsonObject = new JSONObject();
                if(tuple.getStringByField("ch_id").equals(channel)){
                    jsonObject.put("channel", channel);
                    jsonObject.put("count", tuple.getStringByField("count"));
                    jsonObject.put("sum", tuple.getStringByField("sum"));
                    jedis.set("cache-CountSum-" + channel + "-" + this.Sliding, jsonObject.toString());
                }
                else {
                    jsonObject.put("channel", channel);
                    jsonObject.put("count", "0");
                    jsonObject.put("sum", "0");
                    jedis.set("cache-CountSum-" + channel + "-" + this.Sliding, jsonObject.toString());
                }
            }

            jedis.disconnect();

            //collector
        }catch (Exception e){e.printStackTrace();}
    }
}
