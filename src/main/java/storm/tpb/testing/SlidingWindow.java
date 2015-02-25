package storm.tpb.testing;

import org.json.JSONObject;
import org.mortbay.util.ajax.JSON;
import redis.clients.jedis.Jedis;
import storm.tpb.topology.PARAM;
import storm.tpb.topology.TopologyControl;
import storm.tpb.topology.TopologyMain;
import storm.tpb.util.Properties;
import storm.tpb.tools.function;
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

    private List<String> ChannelCode = new ArrayList<String>();
    private List<String> TransactionCode = new ArrayList<String>();
    private List<JSONObject> TotalJson = new ArrayList<JSONObject>();
    private long window;
    private long alphaWindow;
    private long last=System.currentTimeMillis();
    private long lastChart=System.currentTimeMillis();
    private long lastAccDep=System.currentTimeMillis();
    private long lastAccWit=System.currentTimeMillis();
    private long lastAccTran=System.currentTimeMillis();
    private double average;
    private double alpha = -1D;
    private boolean sliding = false;
    List<Transaction> listTrans = new ArrayList<Transaction>();
    ArrayList<Long> cacheTime = new ArrayList<Long>();
    List<Transaction> listTransChart = new ArrayList<Transaction>();
    ArrayList<Long> cacheTimeChart = new ArrayList<Long>();
    List<Transaction> listTransAccDep = new ArrayList<Transaction>();
    ArrayList<Long> cacheTimeAccDep = new ArrayList<Long>();
    List<Transaction> listTransAccWit = new ArrayList<Transaction>();
    ArrayList<Long> cacheTimeAccWit = new ArrayList<Long>();
    List<Transaction> listTransAccTran = new ArrayList<Transaction>();
    ArrayList<Long> cacheTimeAccTran = new ArrayList<Long>();
    List<TransactionCount> listTransCount = new ArrayList<TransactionCount>();
    ArrayList<Long> cacheTimeCount = new ArrayList<Long>();
    private List<TransactionTotal> listTotal = new ArrayList<TransactionTotal>();

    private long count=0;
    private long sumAmount=0;
    private long countBranch1=0;
    private long countBranch2=0;
    private long countBranch3=0;
    private long countCenter=0;
    private long sumBranch1=0;
    private long sumBranch2=0;
    private long sumBranch3=0;
    private long sumCenter=0;
    private List<String> TopFiveDep = new ArrayList<String>();
    private List<String> BotFiveDep = new ArrayList<String>();
    private List<String> TopFiveWit = new ArrayList<String>();
    private List<String> BotFiveWit = new ArrayList<String>();;
    private List<String> TopFiveTran = new ArrayList<String>();
    private List<String> BotFiveTran = new ArrayList<String>();;

    public SlidingWindow() {
        this.ChannelCode = function.GetListMongo(Properties.getString("MongoDB.Channel"), "ChannelCode");
        this.TransactionCode = function.GetListMongo(Properties.getString("MongoDB.TransactionTypes"), "TransactionCode");
    }
    public SlidingWindow sliding(double count, Time time) {
        return this.sliding((long) (time.getTime() * count));
    }
    public SlidingWindow sliding(long window) {

            this.sliding = true;
            this.window = window;
            AddCount();
        return this;
    }
    public synchronized void AddCount(){
        try {
            Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            long lenghtRedis = jedis.llen("real-time-count-chart-" + Long.toString(this.window));
            if (lenghtRedis > 0) {
                List<String> list = jedis.lrange("real-time-count-chart-" + Long.toString(this.window), 0, lenghtRedis);
                for (String a : list) {
                    JSONObject jsonObj = new JSONObject(a);
                    this.listTransCount.add(new TransactionCount(jsonObj.getLong("time")));
                }
        }
        }catch (Exception e){
            e.printStackTrace();
        }
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

    public void TotalCountAmount(String channel, long timetamp, long amount) {
        TotalCountAmount(System.currentTimeMillis(), channel, timetamp, (int) amount);
    }
    public synchronized void TotalCountAmount(long time, String channel, long timetamp, int amount) {
        try {
            if (!channel.equals(PARAM.Channel.CHANNELFAKE.getValue())) {
                cacheTimeChart.add(time);
                Transaction tran = new Transaction();
                tran.settimetamp(timetamp);
                tran.setch_id(channel);
                tran.setamount(amount);
                listTransChart.add(tran);
                if (this.sliding) {
                    if ((time - this.lastChart) > this.window) {
                        this.lastChart = time - this.window;
                    }
                }
            }
            Integer a = 0;

            if (!listTransChart.isEmpty())
                while (listTransChart.get(0).gettimetamp() < this.lastChart) {
                    a++;
                    listTransChart.remove(0);
                    System.out.println("XOA" + a.toString());
                    if (listTransChart.isEmpty())
                        break;
                }

            JSONObject obj = new JSONObject();
            List<TransactionTotal> listTransTotal= new ArrayList<TransactionTotal>();
            List<TransactionTotal> asList = new ArrayList<TransactionTotal>();
            if (!listTransChart.isEmpty()) {
                for (int i = 0; i < listTransChart.size(); i++) {
                    listTransTotal.add(new TransactionTotal(listTransChart.get(i).getamount(), listTransChart.get(i).getch_id(), 0));
                }

                HashMap<String, TransactionTotal> aggregate = new HashMap<String, TransactionTotal>();
                //int counts = 0;
                for (TransactionTotal as : listTransTotal) {
                    String key = as.getchannel();
                    TransactionTotal existing = aggregate.get(key);
                    if (existing == null) {
                        as.setcount(1);
                        aggregate.put(key, as);
                        //continue;
                    }else {
                        long counts = aggregate.get(key).getcount();
                        counts++;

                        TransactionTotal combined = new TransactionTotal(as.getamount() + existing.getamount(), as.getchannel(), counts);
                        aggregate.put(key, combined);
                    }
                }

                asList = new ArrayList<TransactionTotal>(aggregate.values());
            }
            
            this.listTotal = asList;
            if(listTransChart.isEmpty())
                this.lastChart = time - this.window;
            else
                this.lastChart = listTransChart.get(0).gettimetamp();

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void chartFlot(List<TransactionTotal> listTotal) {
        chartFlot(System.currentTimeMillis(), listTotal);
    }
    public synchronized void chartFlot(long time, List<TransactionTotal> listTotal) {
        try {
            cacheTimeCount.add(time);
            TransactionCount tran = new TransactionCount();
            tran.settimestamp(time);
            tran.setListTotal(listTotal);
            listTransCount.add(tran);

            if (this.sliding) {
                if ((time - this.lastChart) > this.window) {
                    this.lastChart = time - this.window;
                }
            }
            Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
            if(!listTransCount.isEmpty()) {
                while (listTransCount.get(0).gettimestamp() < this.lastChart) {
                    listTransCount.remove(0);
                    jedis.blpop(0, "real-time-count-chart-" + Long.toString(this.window));
                    if (listTransCount.isEmpty())
                        break;
                }
            }


            if (!listTransCount.isEmpty()) {
                JSONObject obj = new JSONObject();
                for(TransactionTotal a : listTransCount.get(listTransCount.size() - 1).getListTotal()) {
                    obj.put(a.getchannel(), a.getcount());
                }
                obj.put("time", listTransCount.get(listTransCount.size() - 1).gettimestamp());
                jedis.rpush("real-time-count-chart-" + Long.toString(this.window), obj.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void listAmountAcc(String TranType, long amount, String account, long timetamp) {
        listAmountAcc(TranType, System.currentTimeMillis(), (int) amount, account, timetamp);
    }
    public synchronized void listAmountAcc(String TranType, long time, int amount, String account, long timetamp) {
        if(TranType.equals(PARAM.TransCode.TRANTYPEFAKE.getValue()) || TranType.equals(PARAM.TransCode.DEPOSIT.getValue())) {
            if (!TranType.equals(PARAM.TransCode.TRANTYPEFAKE.getValue())) {
                cacheTimeAccDep.add(time);
                Transaction tran = new Transaction();
                tran.settimetamp(timetamp);
                tran.setacc_no(account);
                tran.setamount(amount);
                listTransAccDep.add(tran);
            }
            if (this.sliding) {
                if ((time - this.lastAccDep) > this.window) {
                    this.lastAccDep = time - this.window;
                }
            }
            Integer a = 0;

            if (!listTransAccDep.isEmpty())
                while (listTransAccDep.get(0).gettimetamp() < this.lastAccDep) {
                    a++;
                    listTransAccDep.remove(0);
                    System.out.println("XOA" + a.toString());
                    if (listTransAccDep.isEmpty())
                        break;
                }
            List<TransactionAcc> listTransAccTotalAmount = new ArrayList<TransactionAcc>();
            List<TransactionAcc> asList = new ArrayList<TransactionAcc>();
            ;
            if (!listTransAccDep.isEmpty()) {
                for (int i = 0; i < listTransAccDep.size(); i++) {
                    listTransAccTotalAmount.add(new TransactionAcc(listTransAccDep.get(i).getamount(), listTransAccDep.get(i).getacc_no()));
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
            TopFiveDep.clear();
            BotFiveDep.clear();
            if (!asList.isEmpty()) {
                for (int i = 0; i < 5 && i < asList.size(); i++)
                    BotFiveDep.add(asList.get(i).getacc_no() + "," + asList.get(i).getamount().toString());
                for (int i = asList.size() - 1; i >= asList.size() - 5 && i >= 0; i--)
                    TopFiveDep.add(asList.get(i).getacc_no() + "," + asList.get(i).getamount().toString());

            }

            if (!listTransAccDep.isEmpty())
                this.lastAccDep = listTransAccDep.get(0).gettimetamp();
            else
                this.lastAccDep = time - this.window;
        }
        if(TranType.equals(PARAM.TransCode.TRANTYPEFAKE.getValue()) || TranType.equals(PARAM.TransCode.WITHDRAWAL.getValue())) {
            cacheTimeAccWit.add(time);

            if (!TranType.equals(PARAM.TransCode.TRANTYPEFAKE.getValue())) {
                Transaction tran = new Transaction();
                tran.settimetamp(timetamp);
                tran.setacc_no(account);
                tran.setamount(amount);
                listTransAccWit.add(tran);
            }
            if (this.sliding) {
                if ((time - this.lastAccWit) > this.window) {
                    this.lastAccWit = time - this.window;
                }
            }
            Integer a = 0;

            if (!listTransAccWit.isEmpty())
                while (listTransAccWit.get(0).gettimetamp() < this.lastAccWit) {
                    a++;
                    listTransAccWit.remove(0);
                    System.out.println("XOA" + a.toString());
                    if (listTransAccWit.isEmpty())
                        break;
                }
            List<TransactionAcc> listTransAccTotalAmount = new ArrayList<TransactionAcc>();
            List<TransactionAcc> asList = new ArrayList<TransactionAcc>();
            ;
            if (!listTransAccWit.isEmpty()) {
                for (int i = 0; i < listTransAccWit.size(); i++) {
                    listTransAccTotalAmount.add(new TransactionAcc(listTransAccWit.get(i).getamount(), listTransAccWit.get(i).getacc_no()));
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
            TopFiveWit.clear();
            BotFiveWit.clear();
            if (!asList.isEmpty()) {
                for (int i = 0; i < 5 && i < asList.size(); i++)
                    BotFiveWit.add(asList.get(i).getacc_no() + "," + asList.get(i).getamount().toString());
                for (int i = asList.size() - 1; i >= asList.size() - 5 && i >= 0; i--)
                    TopFiveWit.add(asList.get(i).getacc_no() + "," + asList.get(i).getamount().toString());

            }

            if (!listTransAccWit.isEmpty())
                this.lastAccWit = listTransAccWit.get(0).gettimetamp();
            else
                this.lastAccWit = time - this.window;
        }
        if(TranType.equals(PARAM.TransCode.TRANTYPEFAKE.getValue()) || TranType.equals(PARAM.TransCode.TRANSFERFROM.getValue())) {
            cacheTimeAccTran.add(time);

            if (!TranType.equals(PARAM.TransCode.TRANTYPEFAKE.getValue())) {
                Transaction tran = new Transaction();
                tran.settimetamp(timetamp);
                tran.setacc_no(account);
                tran.setamount(amount);
                listTransAccTran.add(tran);
            }
            if (this.sliding) {
                if ((time - this.lastAccTran) > this.window) {
                    this.lastAccTran = time - this.window;
                }
            }
            Integer a = 0;

            if (!listTransAccTran.isEmpty())
                while (listTransAccTran.get(0).gettimetamp() < this.lastAccTran) {
                    a++;
                    listTransAccTran.remove(0);
                    System.out.println("XOA" + a.toString());
                    if (listTransAccTran.isEmpty())
                        break;
                }
            List<TransactionAcc> listTransAccTotalAmount = new ArrayList<TransactionAcc>();
            List<TransactionAcc> asList = new ArrayList<TransactionAcc>();
            if (!listTransAccTran.isEmpty()) {
                for (int i = 0; i < listTransAccTran.size(); i++) {
                    listTransAccTotalAmount.add(new TransactionAcc(listTransAccTran.get(i).getamount(), listTransAccTran.get(i).getacc_no()));
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
            TopFiveTran.clear();
            BotFiveTran.clear();
            if (!asList.isEmpty()) {
                for (int i = 0; i < 5 && i < asList.size(); i++)
                    BotFiveTran.add(asList.get(i).getacc_no() + "," + asList.get(i).getamount().toString());
                for (int i = asList.size() - 1; i >= asList.size() - 5 && i >= 0; i--)
                    TopFiveTran.add(asList.get(i).getacc_no() + "," + asList.get(i).getamount().toString());

            }

            if (!listTransAccTran.isEmpty())
                this.lastAccTran = listTransAccTran.get(0).gettimetamp();
            else
                this.lastAccTran = time - this.window;
        }
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

    public long getSumBranch1() {
        return this.sumBranch1;
    }

    public long getSumBranch2() {
        return this.sumBranch2;
    }

    public long getSumBranch3() {
        return this.sumBranch3;
    }

    public long getSumCenter() {
        return this.sumCenter;
    }

    public List<TransactionTotal> getListTotal() {
        return this.listTotal;
    }

    public List<String> getTopFiveDep(){return this.TopFiveDep; }

    public List<String> getBotFiveDep(){return this.BotFiveDep; }

    public List<String> getTopFiveWit(){return this.TopFiveWit; }

    public List<String> getBotFiveWit(){return this.BotFiveWit; }

    public List<String> getTopFiveTran(){return this.TopFiveTran; }

    public List<String> getBotFiveTran(){return this.BotFiveTran; }

    public List<String> getTransactionCode(){return this.TransactionCode; }

    public List<String> getChannelCode(){return this.ChannelCode; }

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

    public class TransactionTotal {
        private Integer amount;
        private String channel;
        private long count;
        public TransactionTotal(){
        }
        public TransactionTotal(Integer amount,String channel, long count){
            this.amount = amount;
            this.channel = channel;
            this.count = count;
        }
        public Integer getamount()
        {
            return amount;
        }
        public void setamount(Integer amount)
        {
            this.amount = amount;
        }

        public long getcount()
        {
            return count;
        }
        public void setcount(long count)
        {
            this.count = count;
        }

        public String getchannel()
        {
            return channel;
        }
        public void setchannel(String channel)
        {
            this.channel = channel;
        }
    }

    public class TransactionCount implements Serializable {
        private List<TransactionTotal> listTotal;
        private Long timestamp;
        public TransactionCount(){

        }
        public TransactionCount(Long timestamp){
            this.listTotal = new ArrayList<TransactionTotal>();
            this.timestamp = timestamp;
        }
        public TransactionCount(Long timestamp, List<TransactionTotal> listTotal){
            this.listTotal = listTotal;
            this.timestamp = timestamp;
        }
        public List<TransactionTotal> getListTotal()
        {
            return listTotal;
        }
        public void setListTotal(List<TransactionTotal> listTotal)
        {
            this.listTotal = listTotal;
        }

        public Long gettimestamp()
        {
            return timestamp;
        }
        public void settimestamp(Long timestamp)
        {
            this.timestamp = timestamp;
        }


    }
}