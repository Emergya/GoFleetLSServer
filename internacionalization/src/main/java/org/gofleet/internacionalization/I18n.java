/*
 * Copyright (C) 2012, Emergya (http://www.emergya.com)
 *
 * @author <a href="mailto:marias@emergya.com">María Arias de Reyna</a>
 *
 * This file is part of GoFleetLS
 *
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
package org.gofleet.internacionalization;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author marias
 * 
 */
@Repository
public class I18n {

	protected String defaultBundle = "i18n/i18n_string";

	@Autowired
	private org.gofleet.configuration.Configuration configuration;

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(
			org.gofleet.configuration.Configuration configuration) {
		this.configuration = configuration;
	}

	public I18n() {
		if (configuration != null)
			defaultBundle = configuration.get("RESOURCE_BUNDLE", defaultBundle);
	}

	public I18n(String bundle) {
		this();
		this.defaultBundle = bundle;
	}

	public String getString(Locale locale, String key) {
		try {
			if (locale == null)
				return getString(key);
			return getResourceBundle(locale).getString(key);
		} catch (Throwable e) {
			return key;
		}
	}

	public String getString(String key) {
		try {
			return getResourceBundle(Locale.ROOT).getString(key);
		} catch (Throwable e) {
			return key;
		}
	}

	public String getString(Locale locale, String key, Object... params) {
		try {
			return MessageFormat.format(getString(locale, key), params);
		} catch (Throwable e) {
			return key;
		}
	}

	/**
	 * @param locale
	 * @return
	 */
	private ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle(defaultBundle, locale);
	}

}
