package storm.tpb.bolts;

import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.List;

public class SaveCountTranBolt extends BaseFunction{
    private double Sliding;
    public SaveCountTranBolt(double Sliding){
        this.Sliding = Sliding;
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
                    jsonAll.put(jsonObject.getString("trx_code") + "-count", jsonObject.getString("count"));
                }
            }
            jsonAll.put("time", System.currentTimeMillis());

            jedis.rpush("real-time-count-tran-" + (long)this.Sliding, jsonAll.toString());
            jedis.disconnect();
            System.out.println("done SaveCountTranBolt");
        }catch (Exception e){e.printStackTrace();}
    }
}
