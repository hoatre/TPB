package storm.tpb.topology;

import java.io.Serializable;

/**
 * Created by Administrator on 1/14/2015.
 */
public class PARAM implements Serializable  {
    public static enum Channel {
        BRANCH1("Branch 1"), BRANCH2("Branch 2"), BRANCH3("Branch 3"), BRANCH4("Contact Center");
        private String value;
        private Channel(String value) {
            this.value = value;
        }
    }
}
