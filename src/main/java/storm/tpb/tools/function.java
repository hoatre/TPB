package storm.tpb.tools;

import com.mongodb.*;
import storm.tpb.util.Properties;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by quangnb on 2/8/15.
 */
public class function {
    public static List<String> GetListMongo(String tableName, String field){
        try {
            List<String> rs = new ArrayList<String>();
            MongoClient mongo = new MongoClient(Properties.getString("MongoDB.host"), Properties.getInt("MongoDB.port"));
            DB db = mongo.getDB(Properties.getString("MongoDB.Name"));
            DBCollection table = db.getCollection(tableName);
            BasicDBObject allQuery = new BasicDBObject();
            BasicDBObject fields = new BasicDBObject();
            fields.put(field, 1);
            DBCursor cursor = table.find(allQuery, fields);
            while (cursor.hasNext()) {
                rs.add(cursor.next().get(field).toString());
            }
            return rs;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return new ArrayList<String>();
        } catch (MongoException e) {
            e.printStackTrace();
            return new ArrayList<String>();
        }
    }
}
