package storm.tpb.testing;

import redis.clients.jedis.Jedis;
import storm.tpb.topology.PARAM;
import storm.tpb.topology.TopologyControl;
import storm.tpb.topology.TopologyMain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by quangnb on 1/20/15.
 */
public class SlidingWindow implements Serializable {
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
    // Unix load average-style
    public static final double ONE_MINUTE_ALPHA = 1 - Math.exp(-5d /
            60d / 1d);
    public static final double FIVE_MINUTE_ALPHA = 1 - Math.exp(-5d /
            60d / 5d);
    public static final double FIFTEEN_MINUTE_ALPHA = 1 - Math.exp(-5d
            / 60d / 15d);


    private long window;
    private long alphaWindow;
    private long last=System.currentTimeMillis();
    private long lastChart=System.currentTimeMillis();
    private long lastAcc=System.currentTimeMillis();
    private double average;
    private double alpha = -1D;
    private boolean sliding = false;
    List<Transaction> listTrans = new ArrayList<Transaction>();
    ArrayList<Long> cacheTime = new ArrayList<Long>();
    List<Transaction> listTransChart = new ArrayList<Transaction>();
    ArrayList<Long> cacheTimeChart = new ArrayList<Long>();
    List<Transaction> listTransAcc = new ArrayList<Transaction>();
    ArrayList<Long> cacheTimeAcc = new ArrayList<Long>();
    private long count=0;
    private long sumAmount=0;
    private long countBranch1=0;
    private long countBranch2=0;
    private long countBranch3=0;
    private long countCenter=0;
    private List<String> TopFive = new ArrayList<String>();
    private List<String> BotFive = new ArrayList<String>();;

    public SlidingWindow() {
    }
    public SlidingWindow sliding(double count, Time time) {
        return this.sliding((long) (time.getTime() * count));
    }
    public SlidingWindow sliding(long window) {
        this.sliding = true;
        this.window = window;
        return this;
    }
    public SlidingWindow withAlpha(double alpha) {
        if (!(alpha > 0.0D && alpha <= 1.0D)) {
            throw new IllegalArgumentException("Alpha must be between 0.0 and 1.0");
        }
        this.alpha = alpha;
        return this;
    }
    public SlidingWindow withAlphaWindow(long alphaWindow) {
        this.alpha = -1;
        this.alphaWindow = alphaWindow;
        return this;
    }
    public SlidingWindow withAlphaWindow(double count, Time time) {
        return this.withAlphaWindow((long) (time.getTime() * count));
    }
    public void mark(long amount, long timetamp) {
        mark(System.currentTimeMillis(),(int)amount, timetamp);
    }
    public synchronized void mark(long time, int amount, long timetamp) {
        cacheTime.add(time);
        Transaction tran = new Transaction();
        tran.settimetamp(timetamp);
        tran.setamount(amount);
        listTrans.add(tran);
        if (this.sliding) {
            if ((time - this.last) > this.window) {
                this.last = time-this.window;
            }
        }
        Integer a=0;

        while (listTrans.get(0).gettimetamp() < this.last) {
            a++;
            listTrans.remove(0);
            System.out.println("XOA" + a.toString());
        }

        count = listTrans.size();
        sumAmount = 0;
        for (int j=0; j< count;j++)
        {
            if(listTrans.get(j).getamount() > 0 && listTrans.get(j).getamount() != null )
                sumAmount = sumAmount + listTrans.get(j).getamount();
        }
        this.last = listTrans.get(0).gettimetamp();

    }

    public void chart(String channel, long timetamp) {
        chart(System.currentTimeMillis(), channel, timetamp);
    }
    public synchronized void chart(long time, String channel, long timetamp) {
        cacheTimeChart.add(time);
        Transaction tran = new Transaction();
        tran.settimetamp(timetamp);
        tran.setch_id(channel);
        listTransChart.add(tran);
        if (this.sliding) {
            if ((time - this.lastChart) > this.window) {
                this.lastChart = time-this.window;
            }
        }
        Integer a=0;

        while (listTransChart.get(0).gettimetamp() < this.lastChart) {
            a++;
            listTransChart.remove(0);
            System.out.println("XOA" + a.toString());
        }

        countBranch1=0;
        countBranch2=0;
        countBranch3=0;
        countCenter=0;

        if(!listTransChart.isEmpty())
        {
            for(int i = 0; i < listTransChart.size(); i++)
            {
                if(listTransChart.get(i).getch_id().equals(PARAM.Channel.BRANCH1.getValue()))
                    countBranch1++;
                if(listTransChart.get(i).getch_id().equals(PARAM.Channel.BRANCH2.getValue()))
                    countBranch2++;
                if(listTransChart.get(i).getch_id().equals(PARAM.Channel.BRANCH3.getValue()))
                    countBranch3++;
                if(listTransChart.get(i).getch_id().equals(PARAM.Channel.BRANCH4.getValue()))
                    countCenter++;
            }
        }

        this.lastChart = listTransChart.get(0).gettimetamp();

    }

    public void listAmountAcc(long amount, String account, long timetamp) {
        listAmountAcc(System.currentTimeMillis(), (int)amount, account, timetamp);
    }
    public synchronized void listAmountAcc(long time, int amount, String account, long timetamp) {
        cacheTimeAcc.add(time);
        Transaction tran = new Transaction();
        tran.settimetamp(timetamp);
        tran.setacc_no(account);
        tran.setamount(amount);
        listTransAcc.add(tran);
        if (this.sliding) {
            if ((time - this.lastAcc) > this.window) {
                this.lastAcc = time-this.window;
            }
        }
        Integer a=0;

        while (listTransAcc.get(0).gettimetamp() < this.lastAcc) {
            a++;
            listTransAcc.remove(0);
            System.out.println("XOA" + a.toString());
        }
        List<TransactionAcc> listTransAccTotalAmount = new ArrayList<TransactionAcc>();
        List<TransactionAcc> asList = new ArrayList<TransactionAcc>();;
        if(!listTransAcc.isEmpty()) {
            for (int i = 0; i < listTransAcc.size(); i++)
            {
                listTransAccTotalAmount.add(new TransactionAcc(listTransAcc.get(i).getamount(), listTransAcc.get(i).getacc_no()));
            }

                HashMap<String, TransactionAcc> aggregate = new HashMap<String, TransactionAcc>();
            for (TransactionAcc as : listTransAccTotalAmount) {
                String key = as.getacc_no();
                TransactionAcc existing = aggregate.get(key);
                if (existing == null) {
                    aggregate.put(key, as);
                    continue;
                }
                TransactionAcc combined = new TransactionAcc(as.getamount() + existing.getamount(), as.getacc_no());
                aggregate.put(key, combined);
            }

            asList = new ArrayList<TransactionAcc>(aggregate.values());
        }
        Collections.sort(asList);
        TopFive.clear();
        BotFive.clear();
        if(!asList.isEmpty())
        {
            for(int i = 0; i < 5 && i < asList.size(); i++)
                BotFive.add(asList.get(i).getacc_no() + "," + asList.get(i).getamount().toString());
            for(int i = asList.size() - 1; i >= asList.size() - 5 && i >= 0; i--)
                TopFive.add(asList.get(i).getacc_no() + "," + asList.get(i).getamount().toString());

        }

        this.lastAcc = listTransAcc.get(0).gettimetamp();

    }

    public long getWindow() {
        return this.window;
    }

    public long getCount() {
        return this.count;
    }

    public long getSum() {
        return this.sumAmount;
    }

    public long getCountBranch1() {
        return this.countBranch1;
    }

    public long getCountBranch2() {
        return this.countBranch2;
    }

    public long getCountBranch3() {
        return this.countBranch3;
    }

    public long getCountCenter() {
        return this.countCenter;
    }

    public List<String> getTopFive(){return this.TopFive; }

    public List<String> getBotFive(){return this.BotFive; }

    public class TransactionAcc implements Comparable<TransactionAcc>{
        private Integer amount;
        private String acc_no;
        public TransactionAcc(Integer amount,String acc_no){
            this.amount = amount;
            this.acc_no = acc_no;
        }
        public Integer getamount()
        {
            return amount;
        }
        public void setamount(Integer amount)
        {
            this.amount = amount;
        }

        public String getacc_no()
        {
            return acc_no;
        }
        public void setacc_no(String acc_no)
        {
            this.acc_no = acc_no;
        }

        public int compareTo(TransactionAcc other) {

            long delta = this.getamount() - other.getamount();
            if (delta > 0) {
                return 1;
            }
            else if (delta < 0) {
                return -1;
            }
            else {
                return 0;
            }

        }
    }
}