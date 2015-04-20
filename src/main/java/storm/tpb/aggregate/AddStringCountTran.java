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
public class AddStringCountTran implements CombinerAggregator<List<String>> {
    public AddStringCountTran() {
    }

    @Override
    public List<String> combine(List<String> list, List<String> t1) {
        List<String> newList = new ArrayList<String>(list);
        newList.addAll(t1);

        HashSet hs = new HashSet();
        hs.addAll(newList);
        newList.clear();
        newList.addAll(hs);

        return newList;
    }

    public List<String> init(TridentTuple tuple) {
        try {
            List<String> list = new ArrayList<String>();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("trx_code", tuple.getStringByField("trx_code"));
            jsonObject.put("count", tuple.getLongByField("count"));
            list.add(jsonObject.toString());
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<String> zero() {
        return null;
    }
}
