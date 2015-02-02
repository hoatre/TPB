package storm.tpb.testing;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.json.simple.JSONValue;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by HieuLD on 1/28/15.
 */
public class StoreTransactionToMongoDB extends BaseFunction {


    public void execute(TridentTuple tuple, TridentCollector
            collector) {

        try {
            String json = tuple.getString(0);
            Map<String, Object> map = (Map<String, Object>)
                    JSONValue.parse(json);
            Values values = new Values();
            if(!map.get("acc_no").equals("000-000-00000000")) {
                Mongo mongo = new Mongo(Properties.getString("MongoDB.host"), Properties.getInt("MongoDB.port"));
                DB db = mongo.getDB(Properties.getString("MongoDB.Name"));

                DBCollection collection = db.getCollection("CustomerLogs");

                // convert JSON to DBObject directly
                DBObject dbObject = (DBObject) JSON
                        .parse(json);

                collection.insert(dbObject);
            }
//            DBCursor cursorDoc = collection.find();
//            while (cursorDoc.hasNext()) {
//                //System.out.println(cursorDoc.next());
//            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
