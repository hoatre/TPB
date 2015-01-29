package storm.tpb.testing;

import redis.clients.jedis.Jedis;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by quangnb on 1/22/15.
 */
public class SaveRedisTopBotBolt extends BaseFunction {
    private Jedis jedis;
    private String TranType;
    public SaveRedisTopBotBolt(String TranType){
        this.TranType = TranType;
    }
    public synchronized void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));

            if (!tuple.isEmpty()) {
                for (int z = 1; z <= 5; z++) {
                    jedis.hdel("TopTen" + this.TranType + "-Top" + Integer.toString(z) + "-" + Long.toString(tuple.getLongByField("window")), "Acc", "Amount");
                    jedis.hdel("TopTen" + this.TranType + "-Bot" + Integer.toString(z) + "-" + Long.toString(tuple.getLongByField("window")), "Acc", "Amount");
                }
                List<String> TopFive = (ArrayList<String>) tuple.get(0);
                if (TopFive != null) {
                    for (int i = 0; i < TopFive.size(); i++) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("Acc", TopFive.get(i).substring(0, TopFive.get(i).indexOf(",")));
                        map.put("Amount", TopFive.get(i).substring(TopFive.get(i).lastIndexOf(",") + 1));
                        jedis.hmset("TopTen" + this.TranType + "-Top" + Integer.toString(i + 1) + "-" + Long.toString(tuple.getLongByField("window")), map);
                    }
                }
                List<String> BotFive = (ArrayList<String>) tuple.get(1);
                if (BotFive != null) {
                    for (int i = 0; i < BotFive.size(); i++) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("Acc", BotFive.get(i).substring(0, BotFive.get(i).indexOf(",")));
                        map.put("Amount", BotFive.get(i).substring(BotFive.get(i).lastIndexOf(",") + 1));
                        jedis.hmset("TopTen" + this.TranType + "-Bot" + Integer.toString(i + 1) + "-" + Long.toString(tuple.getLongByField("window")), map);
                    }
                }
            }
        }catch (Exception e){}

    }
}
