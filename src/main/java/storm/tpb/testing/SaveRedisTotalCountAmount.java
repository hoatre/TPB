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
    public synchronized void execute(TridentTuple tuple, TridentCollector collector) {
        //this.sliding.mark(tuple.getLongByField("amount"), tuple.getLongByField("timestamp"));
        //collector.emit(new Values(this.sliding.getCount(),this.sliding.getSum()));
        //System.out.println("Rediscount : " + Long.toString(this.sliding.getCount()) + " sum : " + Long.toString(this.sliding.getSum()));
        jedis=new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
        jedis.set("TotalNoTran", Long.toString(tuple.getLong(0)));
        jedis.set("TotalAmount", Long.toString(tuple.getLong(1)));
       // collector.emit(tuple);
    }
}
