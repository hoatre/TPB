package storm.tpb.bolts;

import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.List;

public class SaveCountSum_2_1_Bolt extends BaseFunction{
    private double Sliding;
    private long SlidingTimeWait = 0;
    public SaveCountSum_2_1_Bolt(double Sliding, long SlidingTimeWait){
        this.Sliding = Sliding;
        this.SlidingTimeWait = SlidingTimeWait;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            jedis.connect();
            List<String> list = new ArrayList<String>();
            if(tuple.getValue(0) != null)
                list = (List<String>)tuple.getValue(0);

            JSONObject jsonAll = new JSONObject();
            if(!list.isEmpty() && list != null) {
                for (String a : list) {
                    JSONObject jsonObject = new JSONObject(a);
                    jsonAll.put(jsonObject.getString("channel") + "-count", jsonObject.getString("count"));
                    jsonAll.put(jsonObject.getString("channel") + "-sum", jsonObject.getString("sum"));
                }
            }
            jsonAll.put("time", System.currentTimeMillis());

            jedis.rpush("real-time-count-chart-window-" + (long)this.Sliding, jsonAll.toString());
            jedis.disconnect();
            System.out.println("done SaveCountSum_2_1_Bolt");
            if(this.SlidingTimeWait == 0)
                Thread.sleep(Properties.getInt("Load.Interval.Time"));
            else
                Thread.sleep(SlidingTimeWait);
        }catch (Exception e){e.printStackTrace();}
    }
}
