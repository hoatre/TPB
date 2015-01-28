package storm.tpb.testing;

import redis.clients.jedis.Jedis;
import storm.tpb.util.Properties;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

/**
 * Created by quangnb on 1/22/15.
 */
public class SaveRedisForChart extends BaseFunction {
    private Jedis jedis;
    public synchronized void execute(TridentTuple tuple, TridentCollector collector) {
        jedis=new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
        jedis.set("real-time-Branch 1-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countBranch1")));
        jedis.set("real-time-Branch 2-"+ Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countBranch2")));
        jedis.set("real-time-Branch 3-"+ Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countBranch3")));
        jedis.set("real-time-Contact Center-" + Long.toString(tuple.getLongByField("window")), Long.toString(tuple.getLongByField("countCenter")));
    }
}
