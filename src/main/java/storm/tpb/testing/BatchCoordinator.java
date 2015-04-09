package storm.tpb.testing;

/**
 * Created by phonghh on 4/6/15.
 */
public interface BatchCoordinator<X> {
    X initializeTransaction(long txid, X prevMetadata);
    void success(long txid);
    boolean isReady(long txid);
    void close();
}
