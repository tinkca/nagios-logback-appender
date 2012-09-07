package in.verse.logback.nagios;

import ch.qos.logback.classic.Level;

public enum NagiosLevel
{

    OFF(-1),
    OK(0),
    WARN(1),
    CRITICAL(2),
    UNKNOWN(3);

    int level;

    NagiosLevel(int level)
    {
        this.level = level;
    }

    public int getLevel()
    {
        return this.level;
    }

    public static NagiosLevel valueOf(Level logLevel)
    {
        switch (logLevel.levelInt)
        {
            case Level.DEBUG_INT:
            case Level.TRACE_INT:
            case Level.INFO_INT:
                return NagiosLevel.OK;
            case Level.WARN_INT:
                return NagiosLevel.WARN;
            case Level.ERROR_INT:
                return NagiosLevel.CRITICAL;
            default:
                return NagiosLevel.OFF;
        }

    }

}
