package storm.tpb.testing;

import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

/**
 * Created by quangnb on 1/22/15.
 */
public class SaveRedisTotalCountAmount extends BaseFunction {
    private Jedis jedis;
    private String channel;
    public SaveRedisTotalCountAmount(String channel){
        this.channel = channel;
    }
    public synchronized void execute(TridentTuple tuple, TridentCollector collector) {
        try {
            jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"), 2000);
            jedis.set("TotalNoTran-" + this.channel + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLong(0)));
            jedis.set("TotalAmount-" + this.channel + "-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLong(1)));
        }catch (Exception e){}
    }
}
