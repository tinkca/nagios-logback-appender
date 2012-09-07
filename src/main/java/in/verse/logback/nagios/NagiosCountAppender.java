package in.verse.logback.nagios;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class NagiosCountAppender extends AppenderBase<ILoggingEvent>
{
    String nscaWebUrl;
    String nscaWebUserName;
    String nscaWebPassword;
    String hostname;
    String servicename;
    String criticalCount;
    int criticalInt;
    String warningCount;
    int warningInt;
    String sendDelay;
    int sendDelayInt;
    String reloadDelay;
    long reloadDelayLong = 0;
    long lastFlushTime;

    public String getNscaWebUrl()
    {
        return nscaWebUrl;
    }

    public void setNscaWebUrl(String nscaWebUrl)
    {
        this.nscaWebUrl = nscaWebUrl;
    }

    public String getNscaWebUserName()
    {
        return nscaWebUserName;
    }

    public void setNscaWebUserName(String nscaWebUserName)
    {
        this.nscaWebUserName = nscaWebUserName;
    }

    public String getNscaWebPassword()
    {
        return nscaWebPassword;
    }

    public void setNscaWebPassword(String nscaWebPassword)
    {
        this.nscaWebPassword = nscaWebPassword;
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public String getServicename()
    {
        return servicename;
    }

    public void setServicename(String servicename)
    {
        this.servicename = servicename;
    }

    public String getCriticalCount()
    {
        return criticalCount;
    }

    public void setCriticalCount(String criticalCount)
    {
        this.criticalCount = criticalCount;
    }

    public String getWarningCount()
    {
        return warningCount;
    }

    public void setWarningCount(String warningCount)
    {
        this.warningCount = warningCount;
    }

    public String getSendDelay()
    {
        return sendDelay;
    }

    public void setSendDelay(String sendDelay)
    {
        this.sendDelay = sendDelay;
    }

    public Map<String, AtomicLong> getExceptionCount()
    {
        return exceptionCount;
    }

    public void setExceptionCount(Map<String, AtomicLong> exceptionCount)
    {
        this.exceptionCount = exceptionCount;
    }

    public String getReloadDelay()
    {
        return reloadDelay;
    }

    public void setReloadDelay(String reloadDelay)
    {
        this.reloadDelay = reloadDelay;
    }

    public long getLastFlushTime()
    {
        return lastFlushTime;
    }

    Map<Integer, AtomicLong> logLevelCount;
    Map<String, AtomicLong> exceptionCount;
    AtomicLong totalCount;
    private boolean firstWarning = true;
    private boolean firstCritial = true;
    Timer timer;
    TimerTask task;

    @Override
    public void start()
    {
        try
        {
            sendDelayInt = Integer.parseInt(sendDelay);
        }
        catch (NumberFormatException e)
        {
            sendDelayInt = 5;
        }
        try
        {
            warningInt = Integer.parseInt(warningCount);
        }
        catch (NumberFormatException e)
        {
            warningInt = 100;
        }
        try
        {
            criticalInt = Integer.parseInt(criticalCount);
        }
        catch (NumberFormatException e)
        {
            criticalInt = 100;
        }

        try
        {
            reloadDelayLong = Long.parseLong(reloadDelay) * 60 * 1000;
        }
        catch (NumberFormatException e)
        {
            reloadDelayLong = 0;
        }

        if (hostname == null || servicename == null || nscaWebUrl == null || nscaWebUserName == null || nscaWebPassword == null)
        {
            addError("Not valid configurations");
            return;
        }

        flush();

        task = new NagiosCountSenderTask(this);
        timer = new Timer();
        timer.schedule(task, 0, sendDelayInt * 60 * 1000);
        super.start();
    }

    public void flush()
    {
        logLevelCount = new ConcurrentHashMap<Integer, AtomicLong>();
        exceptionCount = new ConcurrentHashMap<String, AtomicLong>();
        totalCount = new AtomicLong(0);
        lastFlushTime = System.currentTimeMillis();
        firstWarning = true;
        firstCritial = true;
    }

    @Override
    protected void append(ILoggingEvent event)
    {
        AtomicLong current = logLevelCount.get(event.getLevel().toInteger());
        if (current == null)
        {
            current = new AtomicLong(1);
            logLevelCount.put(event.getLevel().toInteger(), current);
        }
        else
        {
            current.incrementAndGet();
        }

        if (event.getThrowableProxy() != null && event.getLevel().levelInt == Level.ERROR_INT)
        {
            AtomicLong cnt = exceptionCount.get(event.getThrowableProxy().getClassName());
            if (cnt == null)
            {
                cnt = new AtomicLong(1);
                exceptionCount.put(event.getThrowableProxy().getClassName(), cnt);
            }
            else
            {
                cnt.incrementAndGet();
            }
            long count = totalCount.incrementAndGet();
            boolean runThread = false;
            if (firstCritial && count >= criticalInt)
            {
                runThread = true;
                firstCritial = false;
            }
            if (firstWarning && count >= warningInt)
            {
                runThread = true;
                firstWarning = false;
            }

            if (runThread)
            {
                // System.out.println("Running");
                new Thread(task).start();
            }
        }
    }

}
