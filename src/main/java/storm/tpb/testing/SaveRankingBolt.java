package storm.tpb.testing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import storm.tpb.topology.PARAM;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFilter;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * get list sliding
 */
public class SaveRankingBolt extends BaseFunction{
    private double Sliding;
    private int TOP;
    public SaveRankingBolt(int TOP, double Sliding){
        this.TOP = TOP;
        this.Sliding = Sliding;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            List<JSONObject> asList = new ArrayList<JSONObject>();
            if(tuple.getValue(0) != null)
                asList = (List<JSONObject>)tuple.getValue(0);

            Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            jedis.connect();


            if (!asList.isEmpty()) {
                String tranType = asList.get(0).getString("transactionType");
                Collections.sort(asList, new Comparator<JSONObject>() {
                    @Override
                    public int compare(JSONObject a, JSONObject b) {
                        long valA = 0;
                        long valB = 0;

                        try {
                            valA = a.getLong("sum");
                            valB = b.getLong("sum");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        long comp = valA - valB;

                        if (comp > 0)
                            return 1;
                        if (comp < 0)
                            return -1;
                        return 0;
                    }
                });


                JSONObject obj = new JSONObject();
                obj.put("TransactionType", tranType);
                int i = 1;
                for (int j = 0; j < TOP && j < asList.size(); j++) {
                    if (asList.get(j).has("account")) {
                        obj.put("TopTen-Bot" + Integer.toString(i) + "-" + (long) this.Sliding + "-Acc", asList.get(j).getString("account"));
                        obj.put("TopTen-Bot" + Integer.toString(i) + "-" + (long) this.Sliding + "-Amount", asList.get(j).getLong("sum"));
                        i++;
                    }
                }

                int k = 1;
                for (int j = asList.size() - 1; j >= asList.size() - TOP && j >= 0; j--) {
                    if (asList.get(j).has("account")) {
                        obj.put("TopTen-Top" + Integer.toString(k) + "-" + (long) this.Sliding + "-Acc", asList.get(j).getString("account"));
                        obj.put("TopTen-Top" + Integer.toString(k) + "-" + (long) this.Sliding + "-Amount", asList.get(j).getLong("sum"));
                        k++;
                    }
                }
                if (obj != null && !tranType.equals(PARAM.TransCode.TRANTYPEFAKE.getValue()))
                    jedis.set("Ranking-" + tranType + "-" + (long) this.Sliding, obj.toString());
            }
//            }else
//            {
//                JSONObject obj = new JSONObject();
//                obj.put("TransactionType", tranType);
//                jedis.set("Ranking-" + tranType + "-" + (long) this.Sliding, obj.toString());
//            }

            jedis.disconnect();
            System.out.println("done SaveRankingBolt");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
