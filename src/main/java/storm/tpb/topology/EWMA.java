package storm.tpb.topology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by Administrator on 1/13/2015.
 */
public class EWMA implements Serializable {

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
    // Unix load average-style alpha constants
    public static final double ONE_MINUTE_ALPHA = 1 - Math.exp(-5d /
            60d / 1d);
    public static final double FIVE_MINUTE_ALPHA = 1 - Math.exp(-5d /
            60d / 5d);
    public static final double FIFTEEN_MINUTE_ALPHA = 1 - Math.exp(-5d
            / 60d / 15d);
    private long window;
    private long alphaWindow;
    private long last=0;
    private double average;
    private double alpha = -1D;
    private boolean sliding = false;
    private long count=0;
    //private Map<Long , Object> dataCache = new HashMap<Long, Object>();
    ArrayList<Long> cacheTime = new ArrayList<Long>();
    public EWMA() {
    }
    public EWMA sliding(double count, Time time) {
        return this.sliding((long) (time.getTime() * count));
    }
    public EWMA sliding(long window) {
        this.sliding = true;
        this.window = window;
        return this;
    }

    public void mark() {
        mark(System.currentTimeMillis());
    }
    public synchronized void mark(long time) {
        cacheTime.add(time);
        if (this.sliding) {
            if ((time - this.last) > this.window) {
                    this.last = time-this.window;
            }
        }
        Integer a=0;
        System.out.println("time " + String.valueOf(time) + " last " + String.valueOf(last) + " get0 " + String.valueOf(cacheTime.get(0)));
        while (cacheTime.get(0) < this.last) {
            a++;
            cacheTime.remove(0);
            System.out.println("XOA" + a.toString());
        }
        String str="";

        count = cacheTime.size();

        this.last = cacheTime.get(0);
    }


    public long getCount() {
        return this.count;
    }
}



