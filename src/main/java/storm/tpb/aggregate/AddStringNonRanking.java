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
public class AddStringNonRanking implements CombinerAggregator<List<String>> {
    public AddStringNonRanking() {
    }

    public List<String> init(TridentTuple tuple) {
        try {
            List<String> list = new ArrayList<String>();
            list.add(tuple.getStringByField("trx_code"));
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<String> combine(List<String> list, List<String> t1) {
        List<String> newList = new ArrayList<String>(list);
        newList.addAll(t1);

        // remove duplicate
        HashSet hs = new HashSet();
        hs.addAll(newList);
        newList.clear();
        newList.addAll(hs);

        return newList;
    }

    public List<String> zero() {
        return null;
    }
}
