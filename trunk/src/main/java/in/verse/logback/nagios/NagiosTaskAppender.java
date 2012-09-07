package in.verse.logback.nagios;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class NagiosTaskAppender extends NagiosAppender {
	
	private final Marker monitoringMarker = MarkerFactory.getMarker("MONITOR");
	private final String taskset = "taskset";
	private volatile Map<String, Long> lastTaskExecutionTime = new HashMap<String, Long>();
	private NagiosSender sender;
    String nscaWebUrl;
    String nscaWebUserName;
    String nscaWebPassword;
    String hostname;
    String servicename;
    String criticalTaskExecutionLimitInMin;
    String warningTaskExecutionLimitInMin;
    String checkIntervalInMin;
    private long checkInterval;

	public String getNscaWebUrl() {
		return nscaWebUrl;
	}

	public void setNscaWebUrl(String nscaWebUrl) {
		this.nscaWebUrl = nscaWebUrl;
	}

	public String getNscaWebUserName() {
		return nscaWebUserName;
	}

	public void setNscaWebUserName(String nscaWebUserName) {
		this.nscaWebUserName = nscaWebUserName;
	}

	public String getNscaWebPassword() {
		return nscaWebPassword;
	}

	public void setNscaWebPassword(String nscaWebPassword) {
		this.nscaWebPassword = nscaWebPassword;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getServicename() {
		return servicename;
	}

	public void setServicename(String servicename) {
		this.servicename = servicename;
	}

	public String getCriticalTaskExecutionLimitInMin() {
		return criticalTaskExecutionLimitInMin;
	}

	public void setCriticalTaskExecutionLimitInMin(
			String criticalTaskExecutionLimitInMin) {
		this.criticalTaskExecutionLimitInMin = criticalTaskExecutionLimitInMin;
	}

	public String getWarningTaskExecutionLimitInMin() {
		return warningTaskExecutionLimitInMin;
	}

	public void setWarningTaskExecutionLimitInMin(
			String warningTaskExecutionLimitInMin) {
		this.warningTaskExecutionLimitInMin = warningTaskExecutionLimitInMin;
	}

	public String getCheckIntervalInMin() {
		return checkIntervalInMin;
	}

	public void setCheckIntervalInMin(String checkIntervalInMin) {
		this.checkIntervalInMin = checkIntervalInMin;
	}

	private long warningTaskExecutionLimit;
	private long criticalTaskExecutionLimit;
	private final Runnable TaskMonitorDaemon = new Runnable() {
		public void run() {
			while (true) {
				long start = System.nanoTime();
				Set<String> warningList = new HashSet<String>();
				Set<String> criticalList = new HashSet<String>();
				Set<String> normalList = new HashSet<String>();
				long time = System.currentTimeMillis();
				for (Entry<String, Long> entry : lastTaskExecutionTime.entrySet()) {
					if (time - warningTaskExecutionLimit > entry.getValue()) {
						if (time - criticalTaskExecutionLimit > entry.getValue()) {
							criticalList.add(entry.getKey());
						} else {
							warningList.add(entry.getKey());
						}
					} else {
						normalList.add(entry.getKey());
					}
				}
				String command = getNagiosExternalServiceCommand(time, criticalList, warningList, normalList);
				sender.send(command);
				try {
					Thread.sleep(Math.max(6000, checkInterval - ((System.nanoTime()-start)/1000000)));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public void start() {
		try {
			checkInterval = Long.parseLong(checkIntervalInMin) * 60 * 1000;
		} catch (NumberFormatException e) {
			checkInterval = 2 * 60 * 1000;
		}
		try {
			warningTaskExecutionLimit = Integer.parseInt(warningTaskExecutionLimitInMin) * 60 * 1000;
		} catch (NumberFormatException e) {
			warningTaskExecutionLimit = 10 * 60 * 1000;
		}
		try {
			criticalTaskExecutionLimit = Integer.parseInt(criticalTaskExecutionLimitInMin) * 60 * 1000;
		} catch (NumberFormatException e) {
			criticalTaskExecutionLimit = 30 * 60 * 1000;
		}

		if (hostname == null || servicename == null || nscaWebUrl == null 	|| nscaWebUserName == null || nscaWebPassword == null) {
			addError("Not valid configurations");
			return;
		}

		sender = new NagiosSender(nscaWebUrl, nscaWebUserName, nscaWebPassword, hostname, servicename);
        Thread task = new Thread(TaskMonitorDaemon);
        task.setName("TaskMonitorDaemon");
        task.setDaemon(true);
        task.start();
        super.start();
	};

	@Override
	protected void append(ILoggingEvent eventObject) {
		if (monitoringMarker.equals(eventObject.getMarker())) {
			String[] tasks = MDC.get(taskset).split(";");
			for (String task : tasks) {
				if (!lastTaskExecutionTime.keySet().contains(task)) {
					synchronized (lastTaskExecutionTime) {
						lastTaskExecutionTime.put(task, Long.MIN_VALUE);
					}
				}
			}
			synchronized (lastTaskExecutionTime) {
				lastTaskExecutionTime.put(eventObject.getMessage(), System.currentTimeMillis());
			}
		}
	}
	
	private String getNagiosExternalServiceCommand(long SystemTime, Set<String> criticalSet, Set<String> warningSet, Set<String> normalSet)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(SystemTime / 1000).append("]");
        sb.append(" ");
        sb.append("PROCESS_SERVICE_CHECK_RESULT").append(";");
        sb.append(hostname).append(";");
        sb.append(servicename).append(";");
        
        NagiosLevel level;
        String header;
        if (criticalSet.size() > 0) {
        	level = NagiosLevel.CRITICAL;
        	header = Arrays.toString(criticalSet.toArray());
        } else if (warningSet.size() > 0) {
        	level = NagiosLevel.WARN;
        	header = Arrays.toString(warningSet.toArray());
        } else {
        	level = NagiosLevel.OK;
        	header = "All Tasks OK";
        }
        sb.append(level).append(";");
        sb.append(header).append("\\n");
        
        sb.append("[").append(SystemTime / 1000).append("] ADD_SVC_COMMENT;");
        sb.append(hostname).append(";");
        sb.append(servicename).append(";");
        if (criticalSet.size() > 0) {
        	sb.append("CRITICAL TASKS : ").append(Arrays.toString(criticalSet.toArray())).append(";");
        }
        if (warningSet.size() > 0) {
        	sb.append("WARNING TASKS : ").append(Arrays.toString(warningSet.toArray())).append(";");
        } 
        if (normalSet.size() > 0){
        	sb.append("NORMAL TASKS : ").append(Arrays.toString(normalSet.toArray())).append(";");
        }
        sb.append("\n");
        return sb.toString();
    }
}
