package storm.tpb.util;

/**
 * Created by HieuLD on 12/16/14.
 */

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Properties
{
    private static final Logger LOGGER = Logger.getLogger(Properties.class);
    private static Properties singleton;

    private Configuration config;

    private Properties()
    {
        try
        {
            this.config = new PropertiesConfiguration(
                    this.getClass().getResource("/conf.properties"));
        }
        catch (Exception ex)
        {
            LOGGER.fatal("Could not load configuration", ex);
            LOGGER.trace(null, ex);
        }
    }

    private static Properties get()
    {
        if (singleton == null)
            singleton = new Properties();
        return singleton;
    }

    public static String getString(String key)
    {
        return get().config.getString(key);
    }

    public static Integer getInt(String key)
    {
        return get().config.getInt(key);
    }
}