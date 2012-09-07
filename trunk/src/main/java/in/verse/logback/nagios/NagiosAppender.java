package in.verse.logback.nagios;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class NagiosAppender extends AppenderBase<ILoggingEvent>
{

    NagiosSender sender;

    String nscaWebUrl;
    String nscaWebUserName;
    String nscaWebPassword;
    String hostname;
    String servicename;

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

    @Override
    public void start()
    {
        sender = new NagiosSender(nscaWebUrl, nscaWebUserName, nscaWebPassword, hostname, servicename);

        System.out.println("INIT Method called.");

        System.out.println("Host is " + nscaWebUrl);

        super.start();
    }

    @Override
    protected void append(ILoggingEvent event)
    {
        if (!event.getLevel().isGreaterOrEqual(Level.INFO))
        {
            return;
        }
        String message = getNagiosExternalServiceCommand(event);
        sender.send(message);
    }

    private String getNagiosExternalServiceCommand(ILoggingEvent event)
    {
        event.getLevel();
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(event.getTimeStamp() / 1000).append("]");
        sb.append(" ");
        sb.append("PROCESS_SERVICE_CHECK_RESULT").append(";");
        sb.append(hostname).append(";");
        sb.append(servicename).append(";");
        sb.append(NagiosLevel.valueOf(event.getLevel()).getLevel()).append(";");
        sb.append(event.getFormattedMessage()).append("\n");
        return sb.toString();
    }

}
