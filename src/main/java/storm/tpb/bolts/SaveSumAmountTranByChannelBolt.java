package storm.tpb.bolts;

import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.List;

public class SaveSumAmountTranByChannelBolt extends BaseFunction{
    private double Sliding;
    public SaveSumAmountTranByChannelBolt(double Sliding){
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
                    jsonAll.put(list.get(i).getString("trx_code") + "-" + list.get(i).getString("ch_id") + "-sum", list.get(i).getString("sum"));
                }
            }
            jsonAll.put("time", System.currentTimeMillis());

            jedis.rpush("real-time-sum-chart-tran-" + (long)this.Sliding, jsonAll.toString());
            jedis.disconnect();
            System.out.println("done SaveSumAmountTranByChannelBolt");
        }catch (Exception e){e.printStackTrace();}
    }
}
