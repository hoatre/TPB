package storm.tpb.aggregate;

import org.json.JSONObject;
import storm.trident.operation.CombinerAggregator;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by phonghh on 4/8/15.
 */
public class AddStringCountChannelByTran implements CombinerAggregator<List<JSONObject>> {
    public AddStringCountChannelByTran() {
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

    public List<JSONObject> init(TridentTuple tuple) {
        try {
            List<JSONObject> list = new ArrayList<JSONObject>();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("transaction", tuple.getStringByField("trx_code"));
            jsonObject.put("channel", tuple.getStringByField("ch_id"));
            jsonObject.put("count", tuple.getLongByField("count"));
            list.add(jsonObject);
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<JSONObject> zero() {
        return null;
    }
}
