package storm.tpb.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
//import storm.tpb.util.IntRandom;
import storm.tpb.testing.*;
import storm.tpb.util.Properties;
import storm.tpb.util.TupleHelpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Created by HieuLD on 12/26/14.
 */

public class SecondsBolt implements IRichBolt {
    private static final long serialVersionUID = 42L;
    private static final Logger LOGGER =
            Logger.getLogger(storm.tpb.testing.RouterBolt.class);
    private OutputCollector collector;
    private Jedis jedis;

    String host;
    int port;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.host = Properties.getString("redis.host");
        this.port = Properties.getInt("redis.port");
        this.collector=collector;
        reconnect();
    }

    public void cleanup() {}

    public void reconnect() {
        this.jedis = new Jedis(host, port);
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        //declarer.declare(new Fields("type","trx_id", "trx_code", "ch_id", "amount", "acc_no", "prd_id"));
    }

    public void execute(Tuple input)
    {
        Transaction transaction = new Transaction();
        transaction.setch_id(input.getValue(0).toString());
        transaction.setamount(Integer.parseInt(input.getValue(1).toString()));
        LOGGER.debug("Transactions summary");

        jedis.set("real-time-" + transaction.getch_id(), transaction.getamount().toString());
        collector.ack(input);
    }

    static Connection conn; // Create a static global variable
    static Statement st;

    /* Get the database connection function*/
    public static Connection getConnection() {
        Connection con = null;  //create a Connection object is used to connect to the database
        try {
            Class.forName("com.mysql.jdbc.Driver");// Load Mysql data-driver
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/test", "root", "root");// create a data connection
        } catch (Exception e) {
            System.out.println("Database connection failed" + e.getMessage());
        }
        return con; //return the established database connection
    }

    public static void insert(String word,int value) {
        conn = getConnection(); // first to get a connection, that connection to the database
        try {
            String sql = "INSERT INTO words(word,count) VALUES ('"+word+"','"+value+"')";  //  insert data sql statement
            st = (Statement) conn.createStatement();    // create static sql statement used to execute a Statement object
            int count = st.executeUpdate(sql);  // number of operations to perform insert sql statement and return to insert data
            System.out.println("insert the words table " + count + " data"); //output the results of the insert operation
            conn.close();   //close the database connection
        } catch (SQLException e) {
            System.out.println("insert data failed" + e.getMessage());
        }
    }

    public Map<String, Object> getComponentConfiguration() { return null; }
}