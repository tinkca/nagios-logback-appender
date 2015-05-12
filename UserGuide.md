# Introduction #

There are three type of appenders for nagios.
  1. NagiosCountAppender : Stores the counting for exceptions logged in error level or above.
  1. NagiosAppender : Sends messages directly to nagios for level higher than info. Debugs & Trace are not sent.
  1. NagiosTaskAppender : Monitors for thread's status.

# General #
## Steps ##
  1. Add nagios-logback-appender in your pom.


&lt;dependency&gt;


> 

&lt;groupId&gt;

in.verse

&lt;/groupId&gt;


> 

&lt;artifactId&gt;

nagios-logback-appender

&lt;/artifactId&gt;


> 

&lt;version&gt;

1.0

&lt;/version&gt;




&lt;/dependency&gt;


    1. Configure passive services on nagios.
    1. Add appender in your logback xml
> 

&lt;appender name="CUSTOM3" class="in.verse.logback.nagios.NagiosCountAppender"&gt;


> > 

&lt;nscaWebUrl&gt;



&lt;/nscaWebUrl&gt;


> > 

&lt;nscaWebUserName&gt;



&lt;/nscaWebUserName&gt;


> > 

&lt;nscaWebPassword&gt;



&lt;/nscaWebPassword&gt;


> > 

&lt;hostname&gt;



&lt;/hostname&gt;


> > 

&lt;servicename&gt;



&lt;/servicename&gt;


> > 

&lt;sendDelay&gt;



&lt;/sendDelay&gt;


> > 

&lt;warningCount&gt;



&lt;/warningCount&gt;


> > 

&lt;criticalCount&gt;



&lt;/criticalCount&gt;



> 

&lt;/appender&gt;



Host name is your machines name as configured in nagios. Service name is your service name as configured in nagios.
SendDelay is time in minutes when count should be sent to nagios.
warningCount/criticalCount is count of exceptions when nagios should say it warning & critical respectively.
nscaWebUrl is the url were http://wiki.smetj.net/wiki/Nscaweb nsca web queue is running.