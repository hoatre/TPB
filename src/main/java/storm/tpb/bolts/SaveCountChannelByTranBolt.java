package storm.tpb.bolts;

import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SaveCountChannelByTranBolt extends BaseFunction{
    private double Sliding;
    public SaveCountChannelByTranBolt(double Sliding){
        this.Sliding = Sliding;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            jedis.connect();
            List<JSONObject> list = new ArrayList<JSONObject>();
            if(tuple.getValue(0) != null)
                list = (List<JSONObject>)tuple.getValue(0);

            JSONObject jsonAll = new JSONObject();
            if(!list.isEmpty() && list != null) {
                for (int i = 0; i<list.size(); i++) {
                    jsonAll.put(list.get(i).getString("transaction") + "-" + list.get(i).getString("channel") + "-count", list.get(i).getString("count"));
                }
            }
            jsonAll.put("time", System.currentTimeMillis());

            jedis.rpush("real-time-count-chart-tran-" + (long)this.Sliding, jsonAll.toString());
            jedis.disconnect();
            System.out.println("done SaveCountChannelByTranBolt");
        }catch (Exception e){e.printStackTrace();}
    }
}
