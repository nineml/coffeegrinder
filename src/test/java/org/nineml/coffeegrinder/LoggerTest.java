package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.logging.DefaultLogger;
import org.nineml.logging.Logger;

public class LoggerTest {
    @Test
    public void testDefaults() {
        Logger logger = new DefaultLogger();
        Assert.assertEquals(Logger.ERROR, logger.getDefaultLogLevel());
        Assert.assertEquals(Logger.ERROR, logger.getLogLevel("fribble-frabble"));
    }

    @Test
    public void testInvalidDefaultProperty() {
        String p1 = System.getProperty(Logger.defaultLogLevelProperty);
        String p2 = System.getProperty(Logger.logLevelsProperty);

        System.setProperty(Logger.defaultLogLevelProperty, "fred");
        Logger logger = new DefaultLogger();
        logger.readSystemProperties();

        Assert.assertEquals(Logger.ERROR, logger.getDefaultLogLevel());

        if (p1 == null) {
            System.clearProperty(Logger.defaultLogLevelProperty);
        } else {
            System.setProperty(Logger.defaultLogLevelProperty, p1);
        }
        if (p2 == null) {
            System.clearProperty(Logger.logLevelsProperty);
        } else {
            System.setProperty(Logger.logLevelsProperty, p2);
        }
    }

    @Test
    public void testProperties() {
        String p1 = System.getProperty(Logger.defaultLogLevelProperty);
        String p2 = System.getProperty(Logger.logLevelsProperty);

        System.setProperty(Logger.logLevelsProperty, "a:1,b:2,c:fred,d:info,e:warning  f:error,g:debug,h:trace,iii:silent");

        Logger logger = new DefaultLogger();
        logger.readSystemProperties();

        Assert.assertEquals(Logger.ERROR, logger.getLogLevel("a"));
        Assert.assertEquals(Logger.WARNING, logger.getLogLevel("b"));
        Assert.assertEquals(Logger.ERROR, logger.getLogLevel("c"));
        Assert.assertEquals(Logger.INFO, logger.getLogLevel("d"));
        Assert.assertEquals(Logger.WARNING, logger.getLogLevel("e"));
        Assert.assertEquals(Logger.ERROR, logger.getLogLevel("f"));
        Assert.assertEquals(Logger.DEBUG, logger.getLogLevel("g"));
        Assert.assertEquals(Logger.TRACE, logger.getLogLevel("h"));
        Assert.assertEquals(Logger.SILENT, logger.getLogLevel("iii"));

        if (p1 == null) {
            System.clearProperty(Logger.defaultLogLevelProperty);
        } else {
            System.setProperty(Logger.defaultLogLevelProperty, p1);
        }
        if (p2 == null) {
            System.clearProperty(Logger.logLevelsProperty);
        } else {
            System.setProperty(Logger.logLevelsProperty, p2);
        }
    }



}
