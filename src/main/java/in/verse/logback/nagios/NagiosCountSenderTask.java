package in.verse.logback.nagios;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class NagiosCountSenderTask extends TimerTask
{
    NagiosSender sender;
    
    NagiosCountAppender appender;
    
    private boolean isFirstCall = true;
    
    public NagiosCountSenderTask(NagiosCountAppender appr)
    {
        this.appender = appr;
        sender = new NagiosSender(appr.getNscaWebUrl(), appr.getNscaWebUserName(), appr.getNscaWebPassword(), appr.getHostname(), appr.getServicename());
    }

    @Override
    public void run()
    {
        String command = getNagiosExternalServiceCommand();
        sender.send(command);
    }
    
    private String getNagiosExternalServiceCommand()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(System.currentTimeMillis() / 1000).append("]");
        sb.append(" ");
        sb.append("PROCESS_SERVICE_CHECK_RESULT").append(";");
        sb.append(appender.hostname).append(";");
        sb.append(appender.servicename).append(";");
        
        NagiosLevel level;
        if (appender.totalCount.get() >= appender.criticalInt)
        {
            level = NagiosLevel.CRITICAL;
        }
        else if (appender.totalCount.get() >= appender.warningInt)
        {
            level = NagiosLevel.WARN;
        }
        else
        {
            level = NagiosLevel.OK;
        }
        sb.append(level.getLevel()).append(";");
        if (isFirstCall)
        {
            sb.append("STARTED-");
        }
        sb.append(level.name()).append(" ");
        sb.append("Exception count:").append(appender.totalCount.get()).append("\\n");
        for (Map.Entry<String, AtomicLong> entry : appender.exceptionCount.entrySet())
        {
            sb.append(entry.getKey()).append(":").append(entry.getValue().get()).append("\\n");
        }
        sb.append("\n");
        
        if (isFirstCall)
        {
            sb.append("[").append(System.currentTimeMillis() / 1000).append("] ADD_SVC_COMMENT;");
            sb.append(appender.hostname).append(";");
            sb.append(appender.servicename).append(";1;Nagios Appender;Service started.\n"); //1 to persist permanently
            isFirstCall = false;
        }
        
        if (appender.reloadDelayLong > 0)
        {
            long timeDifference = System.currentTimeMillis() - appender.getLastFlushTime();
            if (timeDifference >= appender.reloadDelayLong)
            {
                appender.flush();
            }
        }
        
        //System.out.println(sb.toString());
        return sb.toString();
    }
}
