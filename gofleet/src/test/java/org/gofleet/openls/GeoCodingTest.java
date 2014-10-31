package org.gofleet.openls;

/*
 * Copyright (C) 2011, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:marcos@emergya.es">Moisés Arcos</a>
 *
 * This file is part of GoFleet
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.gofleet.openLS.OpenLS;
import org.gofleet.openLS.handlers.GeocodingHandler;
import org.gofleet.openLS.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.ogc.AbstractBodyType;
import org.jvnet.ogc.AbstractResponseParametersType;
import org.jvnet.ogc.AddressType;
import org.jvnet.ogc.GeocodeResponseListType;
import org.jvnet.ogc.GeocodeResponseType;
import org.jvnet.ogc.GeocodedAddressType;
import org.jvnet.ogc.ResponseType;
import org.jvnet.ogc.ReverseGeocodeResponseType;
import org.jvnet.ogc.ReverseGeocodedLocationType;
import org.jvnet.ogc.XLSType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:openls-*-context.xml" })
public class GeoCodingTest {

	@Autowired
	GeocodingHandler geocoding;
	@Autowired
	OpenLS openLS;

	@Test
	public void testReverseGeocoding() throws FileNotFoundException,
			JAXBException, XMLStreamException, FactoryConfigurationError,
			SAXException {
		XLSType xls = openLS.openLS(Utils.convertFile2XLSType(
				"/reverseGeocoding.xml", XLSType.class));

		assertNotNull("The response is null", xls);

		assertNotNull("The body is null.", xls.getBody());

		List<JAXBElement<? extends AbstractBodyType>> body = xls.getBody();

		assertNotNull("The body is null! How? We have just checked it!", body);

		assertEquals("The body should have one single response", body.size(), 1);

		for (JAXBElement<? extends AbstractBodyType> body_ : body) {
			AbstractBodyType o = body_.getValue();

			assertTrue("This is no response!", o instanceof ResponseType);

			ResponseType response = (ResponseType) o;

			assertNotNull("The contents of the body are null? (ResponseType)",
					response);

			assertEquals("I should have only one response", response
					.getNumberOfResponses().intValue(), 1);

			assertNotNull("Response parameters are null!",
					response.getResponseParameters());

			AbstractResponseParametersType arpt = response
					.getResponseParameters().getValue();

			assertNotNull(arpt);
			assertTrue("The response is not a geocode response",
					arpt instanceof ReverseGeocodeResponseType);

			ReverseGeocodeResponseType res = (ReverseGeocodeResponseType) arpt;

			List<ReverseGeocodedLocationType> res_array = res
					.getReverseGeocodedLocation();
			assertNotNull(res_array);
			assertTrue(res_array.size() > 0);
			for (ReverseGeocodedLocationType locationType : res_array) {
				AddressType addressRes = locationType.getAddress();
				assertNotNull(addressRes.getCountryCode());
				assertNotNull(addressRes.getStreetAddress());
				assertNotNull(addressRes.getStreetAddress().getStreet());
				assertEquals(addressRes.getStreetAddress().getStreet().size(),
						1);
			}
		}
	}

	@Test
	public void testGeocoding() throws FileNotFoundException, JAXBException,
			XMLStreamException, FactoryConfigurationError, SAXException {
		XLSType object = openLS.openLS(Utils.convertFile2XLSType(
				"/geocodingRequest.xml", XLSType.class));

		XLSType xls = (XLSType) object;

		assertNotNull("The response is null", xls);

		assertNotNull("The body is null.", xls.getBody());

		List<JAXBElement<? extends AbstractBodyType>> body = xls.getBody();

		assertNotNull("The body is null! How? We have just checked it!", body);

		for (JAXBElement<? extends AbstractBodyType> body_ : body) {
			AbstractBodyType o = body_.getValue();

			assertTrue("This is no response!", o instanceof ResponseType);

			ResponseType response = (ResponseType) o;

			assertNotNull("The contents of the body are null? (ResponseType)",
					response);

			assertEquals("I should have only one response", response
					.getNumberOfResponses().intValue(), 1);

			assertNotNull("Response parameters are null!",
					response.getResponseParameters());

			AbstractResponseParametersType arpt = response
					.getResponseParameters().getValue();

			assertTrue("The response is not a geocode response",
					arpt instanceof GeocodeResponseType);

			GeocodeResponseType grrt = (GeocodeResponseType) arpt;
			for (GeocodeResponseListType list : grrt.getGeocodeResponseList()) {
				List<GeocodedAddressType> arrayAddress = list
						.getGeocodedAddress();
				assertTrue(arrayAddress.size() > 0);
				for (GeocodedAddressType addressType : arrayAddress) {
					AddressType paramEval = addressType.getAddress();
					assertNotNull(paramEval.getCountryCode());
					assertNotNull(paramEval.getStreetAddress());
					assertNotNull(paramEval.getPlace());
				}
			}
		}
	}
}
