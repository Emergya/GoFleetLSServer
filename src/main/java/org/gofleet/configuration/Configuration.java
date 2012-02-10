package org.gofleet.configuration;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Configuration {

	private static AbstractConfiguration configuration = null;
	private static org.apache.commons.logging.Log log = LogFactory
			.getLog(Configuration.class);

	@Autowired
	private static org.apache.commons.dbcp.BasicDataSource datasource;

	private static AbstractConfiguration getConfiguration() {
		try {
			if (configuration == null)
				configuration = new DatabaseConfiguration(datasource,
						"configuration", "key", "value");
		} catch (Throwable t) {
			log.error("Error loading configuration: " + t);
		}
		return configuration;
	}

	static public String get(String key, String value) {
		try {
			return getConfiguration().getString(key, value);
		} catch (Throwable t) {
			return value;
		}
	}

	static public Boolean get(String key, Boolean value) {
		try {
			return getConfiguration().getBoolean(key, value);
		} catch (Throwable t) {
			return value;
		}
	}
	
	static public Double get(String key, Double value) {
		try {
			return getConfiguration().getDouble(key, value);
		} catch (Throwable t) {
			return value;
		}
	}

	static public Integer get(String key, Integer value) {
		try {
			return getConfiguration().getInteger(key, value);
		} catch (Throwable t) {
			return value;
		}
	}
}
