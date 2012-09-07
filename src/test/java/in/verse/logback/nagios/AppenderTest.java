package in.verse.logback.nagios;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppenderTest
{
    private static final Logger log = LoggerFactory.getLogger(AppenderTest.class);
    
    @Test
    public void test() throws InterruptedException
    {
        Exception e = new RuntimeException("SDSDS");
        log.info("This is info log.");
        log.debug("This is debug log.");
        log.error("This is ERROR log.", e);
        
        for (int i = 0; i <= 5; i++)
        {
            Thread.sleep(500);
            log.error("This is ERROR log.", e);
        }
        
    }

}
