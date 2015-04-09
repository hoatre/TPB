package storm.tpb.testing;

import org.json.JSONException;
import org.json.JSONObject;
import storm.trident.operation.CombinerAggregator;
import storm.trident.tuple.TridentTuple;

import java.util.*;

/**
 * Created by phonghh on 4/8/15.
 */
public class AddStringTopBot implements CombinerAggregator<List<JSONObject>> {
    public AddStringTopBot() {
    }

    public List<JSONObject> init(TridentTuple tuple) {
        try {
            List<JSONObject> list = new ArrayList<JSONObject>();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("transactionType", tuple.getStringByField("trx_code"));
            jsonObject.put("account", tuple.getStringByField("acc_no"));
            jsonObject.put("sum", tuple.getLongByField("sum"));
            list.add(jsonObject);
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<JSONObject> combine(List<JSONObject> list, List<JSONObject> t1) {
        List<JSONObject> newList = new ArrayList<JSONObject>(list);
        newList.addAll(t1);

        HashSet hs = new HashSet();
        hs.addAll(newList);
        newList.clear();
        newList.addAll(hs);

        return newList;
    }

    public List<JSONObject> zero() {
        return null;
    }
}
