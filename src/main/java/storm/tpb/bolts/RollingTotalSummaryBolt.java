package storm.tpb.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import storm.tpb.testing.Transaction;
import storm.tpb.tools.NthLastModifiedTimeTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by HieuLD on 12/25/14.
 */


/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * This bolt performs rolling counts of incoming objects, i.e. sliding window based counting.
 * <p/>
 * The bolt is configured by two parameters, the length of the sliding window in seconds (which influences the output
 * data of the bolt, i.e. how it will count objects) and the emit frequency in seconds (which influences how often the
 * bolt will output the latest window counts). For instance, if the window length is set to an equivalent of five
 * minutes and the emit frequency to one minute, then the bolt will output the latest five-minute sliding window every
 * minute.
 * <p/>
 * The bolt emits a rolling count tuple per object, consisting of the object itself, its latest rolling count, and the
 * actual duration of the sliding window. The latter is included in case the expected sliding window length (as
 * configured by the user) is different from the actual length, e.g. due to high system load. Note that the actual
 * window length is tracked and calculated for the window, and not individually for each object within a window.
 * <p/>
 * Note: During the startup phase you will usually observe that the bolt warns you about the actual sliding window
 * length being smaller than the expected length. This behavior is expected and is caused by the way the sliding window
 * counts are initially "loaded up". You can safely ignore this warning during startup (e.g. you will see this warning
 * during the first ~ five minutes of startup time if the window length is set to five minutes).
 */
public class RollingTotalSummaryBolt extends BaseRichBolt {

    private static final long serialVersionUID = 5537727428628598519L;
    private static final Logger LOG = Logger.getLogger(RollingTotalSummaryBolt.class);
    private static final int NUM_WINDOW_CHUNKS = 5;
    private static final int DEFAULT_SLIDING_WINDOW_IN_SECONDS = NUM_WINDOW_CHUNKS * 60;
    private static final int DEFAULT_EMIT_FREQUENCY_IN_SECONDS = DEFAULT_SLIDING_WINDOW_IN_SECONDS / NUM_WINDOW_CHUNKS;
    private static final String WINDOW_LENGTH_WARNING_TEMPLATE =
            "Actual window length is %d seconds when it should be %d seconds"
                    + " (you can safely ignore this warning during the startup phase)";

//    private final SlidingWindowCounterTotal<Transaction> counter;
//    private final int windowLengthInSeconds;
//    private final int emitFrequencyInSeconds;
    private OutputCollector collector;
    private NthLastModifiedTimeTracker lastModifiedTracker;
    private long last=System.currentTimeMillis();
    private long count=0;
    private long sumAmount=0;
    private Jedis jedis;
    List<Transaction> listTrans = new ArrayList<Transaction>();
    ArrayList<Long> cacheTime = new ArrayList<Long>();
    private long window;
    private long emitwindowFrequencyInSeconds;

    public RollingTotalSummaryBolt() {
        this(DEFAULT_SLIDING_WINDOW_IN_SECONDS, DEFAULT_EMIT_FREQUENCY_IN_SECONDS);
    }

    public  RollingTotalSummaryBolt(int windowLengthInSeconds, int emitFrequencyInSeconds) {
//        this.windowLengthInSeconds = windowLengthInSeconds;
//        this.emitFrequencyInSeconds = emitFrequencyInSeconds;
        this.window = windowLengthInSeconds;
        this.emitwindowFrequencyInSeconds = emitFrequencyInSeconds;
//        counter = new SlidingWindowCounterTotal<Transaction>(deriveNumWindowChunksFrom(this.windowLengthInSeconds,
//                this.emitFrequencyInSeconds));
        }

    private int deriveNumWindowChunksFrom(int windowLengthInSeconds, int windowUpdateFrequencyInSeconds) {
        return windowLengthInSeconds / windowUpdateFrequencyInSeconds;
    }

    @SuppressWarnings("rawtypes")
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        lastModifiedTracker = new NthLastModifiedTimeTracker(deriveNumWindowChunksFrom((int)this.window,
                (int)this.emitwindowFrequencyInSeconds));
    }

    public synchronized void execute(Tuple tuple) {
        long time = System.currentTimeMillis();
        Transaction tran = new Transaction();
        tran.setamount(tuple.getIntegerByField("amount"));
        tran.settimetamp(tuple.getLongByField("timestamp"));
        cacheTime.add(time);
        //Transaction tran = new Transaction();
//        tran.settimetamp(timetamp);
//        tran.setamount(amount);
        listTrans.add(tran);
        //if (this.sliding) {
        if ((time - this.last) > this.window) {
            this.last = time-this.window;
        }
        //}
        Integer a=0;

        while (listTrans.get(0).gettimetamp() < this.last) {
            a++;
            listTrans.remove(0);
            System.out.println("XOA" + a.toString());
        }
        if(!listTrans.isEmpty()) {
            count = listTrans.size();
            sumAmount = 0;
            for (int j = 0; j < count; j++) {
                if (listTrans.get(j).getamount() > 0 && listTrans.get(j).getamount() != null)
                    sumAmount = sumAmount + listTrans.get(j).getamount();
            }
            this.last = listTrans.get(0).gettimetamp();
            System.out.println("count : " + count + " sum : " + sumAmount);
            jedis.set("TotalNoTran", Long.toString(count));
            jedis.set("TotalAmount", Long.toString(sumAmount));
//            ArrayList<Object> tup = new ArrayList<Object>();
//            tup.add(count);
//            tup.add(sumAmount);
//            collector.emit(tup);
        }
    }

//    private void emitCurrentWindowCounts() {
//        Map<Transaction, Long> counts = counter.getCountsThenAdvanceWindow();
//        int actualWindowLengthInSeconds = lastModifiedTracker.secondsSinceOldestModification();
//        lastModifiedTracker.markAsModified();
//        if (actualWindowLengthInSeconds != windowLengthInSeconds) {
//            LOG.warn(String.format(WINDOW_LENGTH_WARNING_TEMPLATE, actualWindowLengthInSeconds, windowLengthInSeconds));
//        }
//        emit(counts, actualWindowLengthInSeconds);
//    }

    private void emit(Map<Transaction, Long> counts, int actualWindowLengthInSeconds) {
        for (Map.Entry<Transaction, Long> entry : counts.entrySet()) {
            Object obj = entry.getKey();
            Long count = entry.getValue();
            collector.emit(new Values(obj, count, actualWindowLengthInSeconds));
        }
    }

    private void countObjAndAck(Tuple tuple) {
        long time = System.currentTimeMillis();
        Transaction tran = new Transaction();
        tran.setamount(tuple.getIntegerByField("amount"));
        tran.settimetamp(tuple.getLongByField("timestamp"));
        cacheTime.add(time);
        //Transaction tran = new Transaction();
//        tran.settimetamp(timetamp);
//        tran.setamount(amount);
        listTrans.add(tran);
        //if (this.sliding) {
            if ((time - this.last) > this.window) {
                this.last = time-this.window;
            }
        //}
        Integer a=0;

        while (listTrans.get(0).gettimetamp() < this.last) {
            a++;
            listTrans.remove(0);
            System.out.println("XOA" + a.toString());
        }
        if(!listTrans.isEmpty()) {
            count = listTrans.size();
            sumAmount = 0;
            for (int j = 0; j < count; j++) {
                if (listTrans.get(j).getamount() > 0 && listTrans.get(j).getamount() != null)
                    sumAmount = sumAmount + listTrans.get(j).getamount();
            }
            this.last = listTrans.get(0).gettimetamp();
            System.out.println("count : " + count + " sum : " + sumAmount);
            jedis.set("TotalNoTran", Long.toString(count));
            jedis.set("TotalAmount", Long.toString(sumAmount));
//            ArrayList<Object> tup = new ArrayList<Object>();
//            tup.add(count);
//            tup.add(sumAmount);
//            collector.emit(tup);
        }
//        Transaction obj = new Transaction();
//        obj.setacc_no(tuple.getStringByField("acc_no"));
//        obj.setamount(tuple.getIntegerByField("amount"));
//        obj.settrx_code(tuple.getStringByField("trx_code"));
//        counter.incrementCountTotal(obj);
//        collector.ack(tuple);
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("obj", "count", "actualWindowLengthInSeconds"));
    }

//    @Override
//    public Map<String, Object> getComponentConfiguration() {
//        Map<String, Object> conf = new HashMap<String, Object>();
//        conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, emitFrequencyInSeconds);
//        return conf;
//    }
}
