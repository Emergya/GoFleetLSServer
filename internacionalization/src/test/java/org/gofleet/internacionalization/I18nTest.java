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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.Test;

/**
 * @author marias
 * 
 */
public class I18nTest {

	/**
	 * Test method for
	 * {@link org.gofleet.internacionalization.I18n#I18n(java.lang.String)}.
	 */
	@Test
	public void testI18n() {
		final String bundle = "Test";
		I18n i18n = new I18n(bundle);
		assertEquals(i18n.defaultBundle, bundle);
	}

	/**
	 * Test method for
	 * {@link org.gofleet.internacionalization.I18n#getString(java.util.Locale, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetStringLocaleString() {
		final String path = "resourceBundles/i18n_string";
		
		I18n i18n = new I18n(path);
		
		assertEquals("Esto es el valor 1", i18n.getString(new Locale("es", "ES"), "value1"));
		assertEquals("Esto es el valor 2", i18n.getString(new Locale("es", "ES"), "value2"));
		assertEquals("Esto es el valor 3",i18n.getString(new Locale("es", "ES"), "value 3"));

		assertEquals("This is value 1", i18n.getString("value1"));
		assertEquals("This is value 2",i18n.getString("value2"));
		assertEquals("This is value 3", i18n.getString("value 3"));
	}
}
