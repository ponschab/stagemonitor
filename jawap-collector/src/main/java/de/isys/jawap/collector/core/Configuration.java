package de.isys.jawap.collector.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.SECONDS;

// TODO save/load from properties file/Preferences/environment variable
public class Configuration {

	private final Log logger = LogFactory.getLog(getClass());
	private Properties properties;
	private ScheduledExecutorService propertiesReloader;
	private ConcurrentMap<String, Object> propertiesCache = new ConcurrentHashMap<String, Object>();

	public Configuration() {
		loadProperties();
		long reloadInterval = getLong("jawap.properties.reloadIntervalSeconds", -1);
		if (reloadInterval > 0) {
			propertiesReloader = Executors.newSingleThreadScheduledExecutor();
			propertiesReloader.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					loadProperties();
				}
			}, reloadInterval, reloadInterval, SECONDS);
		}
	}

	public int getNoOfWarmupRequests() {
		return getInt("jawap.monitor.noOfWarmupRequests", 0);
	}

	public int getWarmupSeconds() {
		return getInt("jawap.monitor.warmupSeconds", 0);
	}

	public boolean isCollectRequestStats() {
		return getBoolean("jawap.monitor.collectRequestStats", true);
	}

	public boolean isRequestTimerEnabled() {
		return getBoolean("jawap.monitor.requestTimerEnabled", true);
	}

	public long getConsoleReportingInterval() {
		return getLong("jawap.reporting.interval.console", 60);
	}

	public boolean reportToJMX() {
		return getBoolean("jawap.reporting.jmx", true);
	}

	public long getGraphiteReportingInterval() {
		return getLong("jawap.reporting.interval.graphite", -1);
	}

	public String getGraphiteHostName() {
		return getString("jawap.reporting.graphite.hostName");
	}

	public int getGraphitePort() {
		return getInt("jawap.reporting.graphite.port", 2003);
	}

	public long getMinExecutionTimeNanos() {
		return getLong("jawap.profiler.minExecutionTimeNanos", 0L);
	}

	public int getCallStackEveryXRequestsToGroup() {
		return getInt("jawap.profiler.callStackEveryXRequestsToGroup", -1);
	}

	public boolean isLogCallStacks() {
		return getBoolean("jawap.profiler.logCallStacks", true);
	}

	public boolean isReportCallStacksToServer() {
		return getBoolean("jawap.profiler.reportCallStacksToServer", false);
	}

	public String getApplicationName() {
		return getString("jawap.applicationName");
	}

	public String getInstanceName() {
		return getString("jawap.instanceName");
	}

	public String getServerUrl() {
		return getString("jawap.serverUrl");
	}

	public Map<Pattern, String> getGroupUrls() {
		return getPatternMap("jawap.groupUrls",
				"/\\d+: /{id}," +
						"(.*).js: *.js," +
						"(.*).css: *.css," +
						"(.*).js: *.jpg," +
						"(.*).jpeg: *.jpeg," +
						"(.*).png: *.png," +
						"^/(\\d)+/([a-z]+-)+[a-z]+/?: /{itemId}/{item-name}");
	}

	private void loadProperties() {
		logger.info("reloading properties");
		properties = new Properties();
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("jawap.properties");
		try {
			properties.load(resourceStream);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				resourceStream.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			propertiesCache.clear();
		}
	}

	private String getString(final String key) {
		return getAndCache(key, null, new PropertyLoader<String>() {
			@Override
			public String load() {
				return properties.getProperty(key);
			}
		});
	}

	private boolean getBoolean(final String key, final boolean defaultValue) {
		return getAndCache(key, defaultValue, new PropertyLoader<Boolean>() {
			@Override
			public Boolean load() {
				return Boolean.parseBoolean(properties.getProperty(key, Boolean.toString(defaultValue)));
			}
		});
	}

	private int getInt(String key, int defaultValue) {
		return (int) getLong(key, defaultValue);
	}

	private long getLong(final String key, final long defaultValue) {
		return getAndCache(key, defaultValue, new PropertyLoader<Long>() {
			@Override
			public Long load() {
				String value = properties.getProperty(key, Long.toString(defaultValue));
				try {
					return Long.parseLong(value);
				} catch (NumberFormatException e) {
					logger.error(e.getMessage(), e);
					return defaultValue;
				}
			}
		});
	}

	private Map<Pattern, String> getPatternMap(final String key, final String defaultValue) {
		return getAndCache(key, null, new PropertyLoader<Map<Pattern, String>>() {
			@Override
			public Map<Pattern, String> load() {
				String patternString = properties.getProperty(key, defaultValue);
				try {
					String[] groups = patternString.split(",");
					Map<Pattern, String> pattenGroupMap = new HashMap<Pattern, String>(groups.length);

					for (String group : groups) {
						group = group.trim();
						String[] keyValue = group.split(":");
						pattenGroupMap.put(Pattern.compile(keyValue[0].trim()), keyValue[1].trim());
					}
					return pattenGroupMap;
				} catch (RuntimeException e) {
					logger.error("Error while parsing groupUrls. Expected format <regex>: <name>[, <regex>: <name>]. " +
							"Actual value: " + patternString, e);
					return Collections.emptyMap();
				}
			}
		});
	}

	private <T> T getAndCache(String key, T defaultValue, PropertyLoader<T> propertyLoader) {
		T result = (T) propertiesCache.get(key);
		if (result == null) {
			result = propertyLoader.load();
			if (result == null) {
				result = defaultValue;
			}
			if (result != null) {
				propertiesCache.put(key, result);
			}
		}
		return result;
	}

	private interface PropertyLoader<T> {
		T load();
	}

}
