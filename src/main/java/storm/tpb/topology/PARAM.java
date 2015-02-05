package storm.tpb.topology;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import storm.tpb.util.Properties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 1/14/2015.
 */
public class PARAM implements Serializable  {

    public static List<String> listChannels = GetMongo(Properties.getString("MongoDB.Channel"),"ChannelName");

    public static List<String> GetMongo(String tableName, String field){
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

    public static enum Channel {
        BRANCH1("B1"), BRANCH2("B2"), BRANCH3("B3"), BRANCH4("Contact"), TOTAL("Total"), CHANNELFAKE("ChannelFake");
        private String value;
        private Channel(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    public static enum TransCode {
        DEPOSIT("DE"), WITHDRAWAL("WI"), TRANSFERFROM("TF"), TRANSFERTO("TT"),BALANCE("BI"), TRANTYPEFAKE("TranTypeFake");
        private String value;
        private TransCode(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    public static enum Time {
        MILLISECONDS(1), SECONDS(1000), MINUTES(SECONDS.getTime() *
                60), HOURS(MINUTES.getTime() * 60), DAYS(HOURS
                .getTime() * 24), WEEKS(DAYS.getTime() * 7);
        private long millis;
        private Time(long millis) {
            this.millis = millis;
        }
        public long getTime() {
            return this.millis;
        }
    }
}
