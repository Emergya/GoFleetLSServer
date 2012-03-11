package org.gofleet.configuration;

import javax.naming.InitialContext;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.apache.commons.configuration.JNDIConfiguration;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
/**
 * 
 * This class retrieves all the configuration from all sources posibles.
 * 
 * Right now, it looks upon (on this order, lowest configurations may override upper):
 * <ul>
 * <li>Database</li>
 * <li>Server context (likecontext.xml on Tomcat)</li>
 * </ul>
 * 
 * @author marias
 *
 */
public class Configuration {

	private static CompositeConfiguration configuration = null;
	private static org.apache.commons.logging.Log log = LogFactory
			.getLog(Configuration.class);

	@Autowired
	private org.apache.commons.dbcp.BasicDataSource dataSource;

	protected AbstractConfiguration getConfiguration() {
		if (configuration == null) {

			configuration = new CompositeConfiguration();

			try {
				if (dataSource != null)
					configuration.addConfiguration(new DatabaseConfiguration(
							dataSource, "configuration", "key", "value"));
			} catch (Throwable t) {
				log.error("Error loading database configuration", t);
			}
			try {
				configuration.addConfiguration(new JNDIConfiguration(
						new InitialContext()));
			} catch (Throwable t) {
				log.error("Error loading jndi configuration", t);
			}
		}

		return configuration;
	}

	/**
	 * @return the datasource
	 */
	public org.apache.commons.dbcp.BasicDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param datasource
	 *            the datasource to set
	 */
	public void setDataSource(org.apache.commons.dbcp.BasicDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String get(String key, String value) {
		try {
			return getConfiguration().getString(key, value);
		} catch (Throwable t) {
			return value;
		}
	}

	public Boolean get(String key, Boolean value) {
		try {
			return getConfiguration().getBoolean(key, value);
		} catch (Throwable t) {
			return value;
		}
	}

	public Double get(String key, Double value) {
		try {
			return getConfiguration().getDouble(key, value);
		} catch (Throwable t) {
			return value;
		}
	}

	public Integer get(String key, Integer value) {
		try {
			return getConfiguration().getInteger(key, value);
		} catch (Throwable t) {
			return value;
		}
	}
}
