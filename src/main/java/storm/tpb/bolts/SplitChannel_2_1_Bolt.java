package storm.tpb.bolts;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.json.simple.JSONValue;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * get list sliding
 */
public class SplitChannel_2_1_Bolt extends BaseFunction{
    private Fields fields;
    List<String> listCache = new ArrayList<String>();
    public SplitChannel_2_1_Bolt(Fields fields) {
        this.fields = fields;
    }
    public void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            List<String> list = new ArrayList<String>();
            if(tuple.getValue(0) != null) {
                list = (ArrayList<String>) tuple.getValue(0);
                if(listCache.isEmpty())
                    listCache = list;
                else{
                    list.removeAll(listCache);
                    listCache = list;
                }
            }
            if(!list.isEmpty()) {
                for (String a : list) {
                    Values values = new Values();
                    Map<String, Object> map = (Map<String, Object>)
                            JSONValue.parse(a);
                    for (int i = 0; i < this.fields.size(); i++) {
                        values.add(map.get(this.fields.get(i)));
                    }
                    collector.emit(values);
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }
}
