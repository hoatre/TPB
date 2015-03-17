package storm.tpb.topology;

import storm.tpb.testing.Transaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public static final double ONE_HOUR_ALPHA = 1 - Math.exp(-5d /
            60d / 60d);
    public static final double ONE_DAY_ALPHA = 1 - Math.exp(-5d
            / 60d / 1440d);
    private long window;
    private long alphaWindow;
    private long last=0;
    private double average;
    private double alpha = -1D;
    private boolean sliding = false;
    private long count=0;
    private long sum=0;
    List<Transaction> listTrans = new ArrayList<Transaction>();
   // Map<String,Long> ranking
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

    public void mark(int amount) {
        mark(System.currentTimeMillis(),amount);

    }
    public synchronized void mark(long time,int amount) {
        cacheTime.add(time);
        Transaction tran = new Transaction();
        tran.settimetamp(time);
        tran.setamount(amount);
        listTrans.add(tran);
        if (this.sliding) {
            if ((time - this.last) > this.window) {
                    this.last = time-this.window;
            }
        }
        Integer a=0;
        System.out.println("time " + String.valueOf(time) + " last " + String.valueOf(last) + " get0 " + String.valueOf(cacheTime.get(0)));
        while (listTrans.get(0).gettimetamp() < this.last) {
            a++;
            listTrans.remove(0);
            System.out.println("XOA" + a.toString());
        }

        count = listTrans.size();
        for (int j=0; j< count;j++)
        {
            if(listTrans.get(j).getamount() > 0 && listTrans.get(j).getamount() != null )
                sum = sum + listTrans.get(j).getamount();
        }
        this.last = listTrans.get(0).gettimetamp();
    }


    public long getCount() {
        return this.count;
    }

    public long getSum() {
        return this.sum;
    }
}



