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
public class JoinToListString implements CombinerAggregator<List<String>> {
    private List<String> Fields = new ArrayList<String>();
    public JoinToListString(List<String> Fields) {
        this.Fields = Fields;
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
            for(String Field : this.Fields) {
                jsonObject.put(Field, tuple.getValueByField(Field));
            }
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
