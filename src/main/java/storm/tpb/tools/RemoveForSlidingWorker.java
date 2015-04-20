package storm.tpb.tools;

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
    private static int i = 0;
    public static void main(String[] args) throws Exception {
        System.out.println("Worker begin");

        CreateTimer((long) PARAM.SlidingTime.Time1.getTime() * 1000, "Sliding-data-", "timestamp");
        CreateTimer((long) PARAM.SlidingTime.Time1.getTime() * 1000, "real-time-count-chart-", "time");
        CreateTimer((long) PARAM.SlidingTime.Time1.getTime() * 1000, "real-time-count-chart-tran-", "time");
        CreateTimer((long) PARAM.SlidingTime.Time1.getTime() * 1000, "real-time-count-tran-", "time");
        CreateTimer((long) PARAM.SlidingTime.Time1.getTime() * 1000, "real-time-count-product-", "time");

        CreateTimer((long) PARAM.SlidingTime.Time2.getTime() * 1000, "Sliding-data-", "timestamp");
        CreateTimer((long) PARAM.SlidingTime.Time2.getTime() * 1000, "real-time-count-chart-", "time");
        CreateTimer((long) PARAM.SlidingTime.Time2.getTime() * 1000, "real-time-count-chart-tran-", "time");
        CreateTimer((long) PARAM.SlidingTime.Time2.getTime() * 1000, "real-time-count-tran-", "time");
        CreateTimer((long) PARAM.SlidingTime.Time2.getTime() * 1000, "real-time-count-product-", "time");

        CreateTimer((long) PARAM.SlidingTime.Time3.getTime() * 1000, "Sliding-data-", "timestamp");
        CreateTimer((long) PARAM.SlidingTime.Time3.getTime() * 1000, "real-time-count-chart-", "time");
        CreateTimer((long) PARAM.SlidingTime.Time3.getTime() * 1000, "real-time-count-chart-tran-", "time");
        CreateTimer((long) PARAM.SlidingTime.Time3.getTime() * 1000, "real-time-count-tran-", "time");
        CreateTimer((long) PARAM.SlidingTime.Time3.getTime() * 1000, "real-time-count-product-", "time");
    }

    private static synchronized void CreateTimer(final long slidingTime,final String key,final String time){
        try{

            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try{
                        long currentTime = System.currentTimeMillis();
                        RemoveRealTimeSlidingData(slidingTime, currentTime, key, time);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            Timer timer = new Timer("MyTimer" + i++);//create a new Timer
            timer.scheduleAtFixedRate(timerTask, 0, 1000);
            System.out.println("Create Timer " + i);
        }catch (Exception e){
            e.printStackTrace();
        }
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
