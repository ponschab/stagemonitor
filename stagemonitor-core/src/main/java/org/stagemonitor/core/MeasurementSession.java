package org.stagemonitor.core;


public class MeasurementSession {

	private final String applicationName;
	private final String hostName;
	private final String instanceName;

	private final String stringRepresentation;

	public MeasurementSession(String applicationName, String hostName, String instanceName) {
		this.applicationName = applicationName;
		this.hostName = hostName;
		this.instanceName = instanceName;
		stringRepresentation = "[application=" + applicationName + "] [instance=" + instanceName + "] [host=" + hostName + "]";
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getHostName() {
		return hostName;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public boolean isInitialized() {
		return applicationName != null && instanceName != null && hostName != null;
	}

	public boolean isNull() {
		return applicationName == null && instanceName == null && hostName == null;
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
