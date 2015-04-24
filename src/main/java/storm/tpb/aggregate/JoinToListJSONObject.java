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
public class JoinToListJSONObject implements CombinerAggregator<List<JSONObject>> {
    private List<String> Fields = new ArrayList<String>();

    public JoinToListJSONObject(List<String> Fields) {
        this.Fields = Fields;
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

    @Override
    public List<JSONObject> init(TridentTuple tuple) {
        try {
            List<JSONObject> list = new ArrayList<JSONObject>();
            JSONObject jsonObject = new JSONObject();
            for(String Field : this.Fields) {
                jsonObject.put(Field, tuple.getValueByField(Field));
            }
            list.add(jsonObject);
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<JSONObject> zero() {
        return null;
    }
}
