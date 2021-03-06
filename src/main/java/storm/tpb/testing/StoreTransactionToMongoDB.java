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

            if(!map.get("acc_no").equals("000-000-00000000") && map.get("acc_no") != null) {
                DBCollection collection = Utils.checkConnection("CustomerLogs");
                DBCollection collectionChannel = Utils.checkConnection("Channels");
                DBCollection collectionTransactionTypes = Utils.checkConnection("TransactionTypes");

                //Query Channel
                BasicDBObject query = new BasicDBObject("ChannelCode", map.get("ch_id"));
                BasicDBObject cursorDoc = (BasicDBObject) collectionChannel.findOne(query);

                //Query Transaction
                BasicDBObject queryTransaction = new BasicDBObject("TransactionCode", map.get("trx_code"));
                BasicDBObject cursorDocTrx = (BasicDBObject) collectionTransactionTypes.findOne(queryTransaction);

                // convert JSON to DBObject directly
                BasicDBObject dbObject = (BasicDBObject) JSON
                        .parse(json);
                dbObject.append("ch_name", cursorDoc.getString("ChannelName"));
                dbObject.append("ch_add", cursorDoc.getString("ChannelAddress"));
                dbObject.append("trx_name", cursorDocTrx.getString("TransactionName"));

                collection.insert(dbObject);
            }
            System.out.println("StoreTransactionToMongoDB");
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
