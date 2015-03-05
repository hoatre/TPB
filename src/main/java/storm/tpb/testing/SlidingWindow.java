package storm.tpb.testing;

import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import storm.tpb.topology.PARAM;
import storm.tpb.util.Properties;
import storm.tpb.tools.function;
import java.io.Serializable;
import java.util.*;

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
    private long lastAcc=System.currentTimeMillis();
    private double average;
    private double alpha = -1D;
    private boolean sliding = false;
    private List<Transaction> listTrans = new ArrayList<Transaction>();
    //private ArrayList<Long> cacheTime = new ArrayList<Long>();
    private List<Transaction> listTransChart = new ArrayList<Transaction>();
    //private ArrayList<Long> cacheTimeChart = new ArrayList<Long>();
    private List<Transaction> listTransAcc = new ArrayList<Transaction>();
    //private ArrayList<Long> cacheTimeAcc = new ArrayList<Long>();
    private List<TransactionCount> listTransCount = new ArrayList<TransactionCount>();
    //rivate ArrayList<Long> cacheTimeCount = new ArrayList<Long>();
    List<TransactionTotal> listTotal = new ArrayList<TransactionTotal>();

    private long count=0;
    private long sumAmount=0;

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
            jedis.connect();
            long lenghtRedis = jedis.llen("real-time-count-chart-" + Long.toString(this.window));
            if (lenghtRedis > 0) {
                List<String> list = jedis.lrange("real-time-count-chart-" + Long.toString(this.window), 0, lenghtRedis);
                for (String a : list) {
                    JSONObject jsonObj = new JSONObject(a);
                    this.listTransCount.add(new TransactionCount(jsonObj.getLong("time")));
                }
            }
            jedis.disconnect();
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
        //cacheTime.add(time);
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
            //System.out.println("XOA" + a.toString());
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

    // Get list sliding
    public void GetlistTotal(String channel, long timetamp, long amount) {
        GetlistTotal(System.currentTimeMillis(), channel, timetamp, (int) amount);
    }
    public synchronized void GetlistTotal(long time, String channel, long timetamp, int amount) {
        try {
            if (!channel.equals(PARAM.Channel.CHANNELFAKE.getValue())) {
                //cacheTimeChart.add(time);
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
                    //System.out.println("XOA" + a.toString());
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
//            listTotal.clear();
//            this.listTotal = asList;

            chartFlot(asList);

            if(listTransChart.isEmpty())
                this.lastChart = time - this.window;
            else
                this.lastChart = listTransChart.get(0).gettimetamp();

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Save redis for chart & total count amount
    public void chartFlot(List<TransactionTotal> listTotal) {
        chartFlot(System.currentTimeMillis(), listTotal);
    }
    public synchronized void chartFlot(long time, List<TransactionTotal> listTotal) {
        try {
            //cacheTimeCount.add(time);
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
            jedis.connect();
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
                    obj.put(a.getchannel() + "-count", a.getcount());
                    obj.put(a.getchannel() + "-sum", a.getamount());
                }

                obj.put("time", listTransCount.get(listTransCount.size() - 1).gettimestamp());
                jedis.rpush("real-time-count-chart-" + Long.toString(this.window), obj.toString());

            }
            jedis.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Ranking acc theo Transaction Type
    public void listAmountAcc(String TranType, long amount, String account, long timetamp) {
        listAmountAcc(TranType, System.currentTimeMillis(), (int) amount, account, timetamp);
    }
    public synchronized void listAmountAcc(String TranType, long time, int amount, String account, long timetamp) {
        try {
            if (!TranType.equals(PARAM.TransCode.TRANTYPEFAKE.getValue())) {
                //cacheTimeAcc.add(time);
                Transaction tran = new Transaction();
                tran.settimetamp(timetamp);
                tran.setacc_no(account);
                tran.setamount(amount);
                tran.settrx_code(TranType);
                listTransAcc.add(tran);
            }
            if (this.sliding) {
                if ((time - this.lastAcc) > this.window) {
                    this.lastAcc = time - this.window;
                }
            }
            Integer a = 0;

            if (!listTransAcc.isEmpty())
                while (listTransAcc.get(0).gettimetamp() < this.lastAcc) {
                    a++;
                    listTransAcc.remove(0);
                    //System.out.println("XOA" + a.toString());
                    if (listTransAcc.isEmpty())
                        break;
                }

            Map<String, List<Transaction>> map = new HashMap<String, List<Transaction>>();
            for (Transaction tran : listTransAcc) {
                String key = tran.gettrx_code();
                if (map.get(key) == null) {
                    map.put(key, new ArrayList<Transaction>());
                }
                map.get(key).add(tran);
            }
            for (String tranType : this.TransactionCode) {
                List<Transaction> listTranGroup = map.get(tranType);
                List<TransactionAcc> listTransAccTotalAmount = new ArrayList<TransactionAcc>();
                List<TransactionAcc> asList = new ArrayList<TransactionAcc>();

                if (listTranGroup != null) {
                    for (int i = 0; i < listTranGroup.size(); i++) {
                        listTransAccTotalAmount.add(new TransactionAcc(listTranGroup.get(i).getamount(), listTranGroup.get(i).getacc_no()));
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
                Jedis jedis = new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
                jedis.connect();
//                for (int z = 1; z <= 5; z++) {
//
//                    jedis.hdel("TopTen-Top" + Integer.toString(z) + "-" + Long.toString(this.window), "Acc", "Amount");
//                    jedis.hdel("TopTen-Bot" + Integer.toString(z) + "-" + Long.toString(this.window), "Acc", "Amount");
//                }
                if (!asList.isEmpty()) {
                    JSONObject obj = new JSONObject();
                    obj.put("TransactionType", tranType);
                    int i = 1;
                    for (int j = 0; j < 5 && j < asList.size(); j++) {

                        //Map<String, String> mapBot = new HashMap<String, String>();

                        obj.put("TopTen-Bot" + Integer.toString(i) + "-" + Long.toString(this.window) + "-Acc", asList.get(j).getacc_no());
                        obj.put("TopTen-Bot" + Integer.toString(i) + "-" + Long.toString(this.window) + "-Amount", asList.get(j).getamount().toString());
                        i++;
//                        if (mapBot != null) {
//                            jedis.hmset("TopTen" + tranType + "-Bot" + Integer.toString(i) + "-" + Long.toString(this.window), mapBot);
//
//                        }
                    }

                    int k = 1;
                    for (int j = asList.size() - 1; j >= asList.size() - 5 && j >= 0; j--) {

                        obj.put("TopTen-Top" + Integer.toString(k) + "-" + Long.toString(this.window) + "-Acc", asList.get(j).getacc_no());
                        obj.put("TopTen-Top" + Integer.toString(k) + "-" + Long.toString(this.window) + "-Amount", asList.get(j).getamount().toString());
                        k++;
//                        if (mapTop != null) {
//                            jedis.hmset("TopTen" + tranType + "-Top" + Integer.toString(k) + "-" + Long.toString(this.window), mapTop);
//
//                        }
                    }
                    if(obj != null && !tranType.equals(PARAM.TransCode.TRANTYPEFAKE.getValue()))
                        jedis.set("Ranking-" + tranType + "-" + Long.toString(this.window), obj.toString());
                }else
                {
                    JSONObject obj = new JSONObject();
                    obj.put("TransactionType", tranType);
                    jedis.set("Ranking-" + tranType + "-" + Long.toString(this.window), obj.toString());
                }
                jedis.disconnect();

            }

            if (!listTransAcc.isEmpty())
                this.lastAcc = listTransAcc.get(0).gettimetamp();
            else
                this.lastAcc = time - this.window;
        }catch (Exception e){
            e.printStackTrace();
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

    public List<TransactionTotal> getListTotal() {
        return this.listTotal;
    }

    public class TransactionAcc implements Comparable<TransactionAcc>{
        private Integer amount;
        private String acc_no;
        //private String Transaction;
        public TransactionAcc(Integer amount,String acc_no){
            this.amount = amount;
            this.acc_no = acc_no;
            //this.Transaction = Transaction;
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
//        public String getTransaction()
//        {
//            return Transaction;
//        }
//        public void setTransaction(String Transaction)
//        {
//            this.Transaction = Transaction;
//        }
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