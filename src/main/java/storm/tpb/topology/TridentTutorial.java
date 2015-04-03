package storm.tpb.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.IMetricsContext;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import jline.internal.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.*;
import storm.trident.spout.ITridentSpout;
import storm.trident.state.State;
import storm.trident.state.StateFactory;
import storm.trident.state.map.IBackingMap;
import storm.trident.state.map.NonTransactionalMap;
import storm.trident.topology.TransactionAttempt;
import storm.trident.tuple.TridentTuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by quangnb on 3/10/15.
 */
public class TridentTutorial {
    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("cdc", conf, buildTopology());
        Thread.sleep(200000);
        cluster.shutdown();
    }
    public static StormTopology buildTopology() {
        TridentTopology topology = new TridentTopology();
        DiagnosisEventSpout spout = new DiagnosisEventSpout();
        Stream inputStream = topology.newStream("event", spout);
        inputStream
                .each(new Fields("event"), new DiseaseFilter())
                .each(new Fields("event"),
                        new CityAssignment(), new Fields("city"))
                        // Derive the hour segment
                .each(new Fields("event", "city"),
                        new HourAssignment(), new Fields("hour",
                                "cityDiseaseHour"))
// Group occurrences in same city and hour
                .groupBy(new Fields("cityDiseaseHour"))
// Count occurrences and persist the results.
                .persistentAggregate(new OutbreakTrendFactory(),
                        new Count(),
                        new Fields("count"))
                .newValuesStream()
// Detect an outbreak
                .each(new Fields("cityDiseaseHour", "count"),
                        new OutbreakDetector(), new Fields("alert"))
// Dispatch the alert
                .each(new Fields("alert"),
                        new DispatchAlert(), new Fields());
        return topology.build();
    }
}

class DiagnosisEventSpout implements ITridentSpout<Long> {
    private static final long serialVersionUID = 1L;
    SpoutOutputCollector collector;
    BatchCoordinator<Long> coordinator = new DefaultCoordinator();
    Emitter<Long> emitter = new DiagnosisEventEmitter();
    @Override
    public BatchCoordinator<Long> getCoordinator(
            String txStateId, Map conf, TopologyContext context) {
        return coordinator;
    }
    @Override
    public Emitter<Long> getEmitter(String txStateId, Map conf,
                                    TopologyContext context) {
        return emitter;
    }
    @Override
    public Map getComponentConfiguration() {
        return null;
    }
    @Override
    public Fields getOutputFields() {
        return new Fields("event");
    }
}

class DefaultCoordinator implements ITridentSpout.BatchCoordinator<Long>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCoordinator.class);
    @Override
    public boolean isReady(long txid) {
        return true;
    }
    @Override
    public void close() {
    }
    @Override
    public Long initializeTransaction(long txid, Long prevMetadata, Long sauMetadata) {
        LOG.info("Initializing Transaction [" + txid + "]");
        return null;
    }
    @Override
    public void success(long txid) {
        LOG.info("Successful Transaction [" + txid + "]");
    }
}

class DiagnosisEventEmitter implements ITridentSpout.Emitter<Long>,
        Serializable {
    private static final long serialVersionUID = 1L;
    AtomicInteger successfulTransactions = new AtomicInteger(0);
    @Override
    public void emitBatch(TransactionAttempt tx, Long
            coordinatorMeta, TridentCollector collector) {
        for (int i = 0; i < 10000; i++) {
            List<Object> events = new ArrayList<Object>();
            double lat = new Double(-30 + (int) (Math.random() * 75));
            double lng =
                    new Double(-120 + (int) (Math.random() * 70));
            long time = System.currentTimeMillis();
            String diag = new Integer(320 +
                    (int) (Math.random() * 7)).toString();
            DiagnosisEvent event =
                    new DiagnosisEvent(lat, lng, time, diag);
            events.add(event);
            collector.emit(events);
        }
    }
    public void success(TransactionAttempt tx) {
        successfulTransactions.incrementAndGet();
    }
    @Override
    public void close() {
    }
}

class DiagnosisEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    public double lat;
    public double lng;
    public long time;
    public String diagnosisCode;
    public DiagnosisEvent(double lat, double lng,
                          long time, String diagnosisCode) {
        super();
        this.time = time;
        this.lat = lat;
        this.lng = lng;
        this.diagnosisCode = diagnosisCode;
    }
}

class DiseaseFilter extends BaseFilter {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
            LoggerFactory.getLogger(DiseaseFilter.class);
    @Override
    public boolean isKeep(TridentTuple tuple) {
        DiagnosisEvent diagnosis = (DiagnosisEvent) tuple.getValue(0);
        Integer code = Integer.parseInt(diagnosis.diagnosisCode);
        if (code.intValue() <= 322) {
            LOG.debug("Emitting disease [" +
                    diagnosis.diagnosisCode + "]");
            return true;
        } else {
            LOG.debug("Filtering disease [" +
                    diagnosis.diagnosisCode + "]");
            return false;
        }
    }
}

class CityAssignment extends BaseFunction {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.
            getLogger(CityAssignment.class);
    private static Map<String, double[]> CITIES =
            new HashMap<String, double[]>();
    { // Initialize the cities we care about.
        double[] phl = { 39.875365, -75.249524 };
        CITIES.put("PHL", phl);
        double[] nyc = { 40.71448, -74.00598 };
        CITIES.put("NYC", nyc);
        double[] sf = { -31.4250142, -62.0841809 };
        CITIES.put("SF", sf);
        double[] la = { -34.05374, -118.24307 };
        CITIES.put("LA", la);
    }
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        DiagnosisEvent diagnosis = (DiagnosisEvent) tuple.getValue(0);
        double leastDistance = Double.MAX_VALUE;
        String closestCity = "NONE";
// Find the closest city.
        for (Map.Entry<String, double[]> city : CITIES.entrySet()) {
            double R = 6371; // km
            double x = (city.getValue()[0] - diagnosis.lng) *
                    Math.cos((city.getValue()[0] + diagnosis.lng) / 2);
            double y = (city.getValue()[1] - diagnosis.lat);
            double d = Math.sqrt(x * x + y * y) * R;
            if (d < leastDistance) {
                leastDistance = d;
                closestCity = city.getKey();
            }
        }
// Emit the value.
        List<Object> values = new ArrayList<Object>();
        values.add(closestCity);
        LOG.debug("Closest city to lat=[" + diagnosis.lat +
                "], lng=[" + diagnosis.lng + "] == ["
                + closestCity + "], d=[" + leastDistance + "]");
        collector.emit(values);
    }
}

class HourAssignment extends BaseFunction {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG =
            LoggerFactory.getLogger(HourAssignment.class);
    @Override
    public void execute(TridentTuple tuple,
                        TridentCollector collector) {
        DiagnosisEvent diagnosis = (DiagnosisEvent) tuple.getValue(0);
        String city = (String) tuple.getValue(1);
        long timestamp = diagnosis.time;
        long hourSinceEpoch = timestamp / 1000 / 60 / 60;
        LOG.debug("Key = [" + city + ":" + hourSinceEpoch + "]");
        String key = city + ":" + diagnosis.diagnosisCode + ":" +
                hourSinceEpoch;
        List<Object> values = new ArrayList<Object>();
        values.add(hourSinceEpoch);
        values.add(key);
        collector.emit(values);
    }
}
class OutbreakDetector extends BaseFunction {
    private static final long serialVersionUID = 1L;
    public static final int THRESHOLD = 10000;
    @Override
    public void execute(TridentTuple tuple,TridentCollector collector) {
        String key = (String) tuple.getValue(0);
        Long count = (Long) tuple.getValue(1);
        if (count > THRESHOLD) {
            List<Object> values = new ArrayList<Object>();
            values.add("Outbreak detected for [" + key + "]!");
            collector.emit(values);
        }
    }
}

class DispatchAlert extends BaseFunction {
    private static final long serialVersionUID = 1L;
    @Override
    public void execute(TridentTuple tuple,
                        TridentCollector collector) {
        String alert = (String) tuple.getValue(0);
        Log.error("ALERT RECEIVED [" + alert + "]");
        Log.error("Dispatch the national guard!");
        System.exit(0);
    }
}
class Count implements CombinerAggregator<Long> {
    @Override
    public Long init(TridentTuple tuple) {
        return 1L;
    }
    @Override
    public Long combine(Long val1, Long val2) {
        return val1 + val2;
    }
    @Override
    public Long zero() {
        return 0L;
    }
}

class OutbreakTrendFactory implements StateFactory {
    private static final long serialVersionUID = 1L;
    @Override
    public State makeState(Map conf, IMetricsContext metrics,
                           int partitionIndex, int numPartitions) {
        return new OutbreakTrendState(new OutbreakTrendBackingMap());
    }
}

class OutbreakTrendState extends NonTransactionalMap<Long> {
    protected OutbreakTrendState(
            OutbreakTrendBackingMap outbreakBackingMap) {
        super(outbreakBackingMap);
    }
}

class OutbreakTrendBackingMap implements IBackingMap<Long> {
    private static final Logger LOG =
            LoggerFactory.getLogger(OutbreakTrendBackingMap.class);
    Map<String, Long> storage =
            new ConcurrentHashMap<String, Long>();
    @Override
    public List<Long> multiGet(List<List<Object>> keys) {
        List<Long> values = new ArrayList<Long>();
        for (List<Object> key : keys) {
            Long value = storage.get(key.get(0));
            if (value==null){
                values.add(new Long(0));
            } else {
                values.add(value);
            }
        }
        return values;
    }
    @Override
    public void multiPut(List<List<Object>> keys, List<Long> vals) {
        for (int i=0; i < keys.size(); i++) {
            LOG.info("Persisting [" + keys.get(i).get(0) + "] ==> ["
                    + vals.get(i) + "]");
            storage.put((String) keys.get(i).get(0), vals.get(i));
        }
    }
}