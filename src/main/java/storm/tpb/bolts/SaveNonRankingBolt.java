package storm.tpb.bolts;

import backtype.storm.tuple.Values;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import scala.util.parsing.combinator.testing.Str;
import storm.tpb.topology.PARAM;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * get list sliding
 */
public class SaveNonRankingBolt extends BaseFunction{
    private double Sliding;
    private List<String> TransactionCode;
    public SaveNonRankingBolt(List<String> TransactionCode, double Sliding){
        this.TransactionCode = TransactionCode;
        this.Sliding = Sliding;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            List<String> list = (ArrayList<String>) tuple.getValue(0);
            List<String> listTo = new ArrayList<String>(this.TransactionCode);
            if(list != null)
                listTo.removeAll(list);
            Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            jedis.connect();
            for(String tranType : listTo){
                JSONObject obj = new JSONObject();
                obj.put("TransactionType", tranType);
                jedis.set("Ranking-" + tranType + "-" + (long) this.Sliding, obj.toString());
            }
            jedis.disconnect();
            System.out.println("done SaveNonRankingBolt");
        }catch (Exception e){e.printStackTrace();}
    }
}
