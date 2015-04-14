package storm.tpb.testing;

import com.mongodb.util.JSON;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import storm.tpb.tools.function;
import storm.tpb.topology.PARAM;
import storm.tpb.util.Properties;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by phonghh on 4/8/15.
 */
public class RemoveForSlidingWorker {
    public static void main(String[] args) throws Exception {
        System.out.println("Worker begin");
        final List<String> TransactionCode = function.GetListMongo(Properties.getString("MongoDB.TransactionTypes"), "TransactionCode");
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                try{


                    long currentTime = System.currentTimeMillis();

                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time1.getTime() * 1000, currentTime, "Sliding-data-", "timestamp");
                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time2.getTime() * 1000, currentTime, "Sliding-data-", "timestamp");
                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time3.getTime() * 1000, currentTime, "Sliding-data-", "timestamp");

                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time1.getTime() * 1000, currentTime, "real-time-count-chart-", "time");
                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time2.getTime() * 1000, currentTime, "real-time-count-chart-", "time");
                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time3.getTime() * 1000, currentTime, "real-time-count-chart-", "time");

                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time1.getTime() * 1000, currentTime, "real-time-count-tran-", "time");
                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time2.getTime() * 1000, currentTime, "real-time-count-tran-", "time");
                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time3.getTime() * 1000, currentTime, "real-time-count-tran-", "time");

                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time1.getTime() * 1000, currentTime, "real-time-count-product-", "time");
                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time2.getTime() * 1000, currentTime, "real-time-count-product-", "time");
                    RemoveRealTimeSlidingData((long) PARAM.SlidingTime.Time3.getTime() * 1000, currentTime, "real-time-count-product-", "time");


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer("MyTimer");//create a new Timer
        timer.scheduleAtFixedRate(timerTask, 0, 2000);
    }

    private static synchronized void RemoveRealTimeSlidingData(long slidingTime, long currentTime,String key, String time){
        try {
            Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            jedis.connect();
            if(jedis.exists(key + slidingTime) && jedis.llen(key + slidingTime) > 0) {
                String jedisFirst = jedis.lindex(key + slidingTime, 0);
                if(jedisFirst != null) {
                    while (new JSONObject(jedisFirst).getLong(time)
                            < currentTime - slidingTime) {
                        jedis.blpop(0, key + slidingTime);
                        jedisFirst = jedis.lindex(key + slidingTime, 0);
                        if (jedisFirst == null)
                            break;
                    }
                }
            }
            jedis.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
