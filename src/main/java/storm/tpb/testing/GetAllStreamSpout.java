package storm.tpb.testing;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.json.simple.JSONValue;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.util.Map;


/**
 * Created by quangnb on 1/20/15.
 */
public class GetAllStreamSpout extends BaseFunction {
    private Fields fields;
    public GetAllStreamSpout(Fields fields) {
        this.fields = fields;
    }
    public void execute(TridentTuple tuple, TridentCollector collector){
        String json = tuple.getString(0);
        Map<String, Object> map = (Map<String, Object>) JSONValue.parse(json);
        Values value = new Values();
        for(int i = 0;i<this.fields.size();i++){
            value.add(map.get(this.fields.get(i)));
        }
        System.out.println("Get : " + value);
        collector.emit(value);
    }
}