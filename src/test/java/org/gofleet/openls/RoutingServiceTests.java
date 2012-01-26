package org.gofleet.openls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import net.opengis.gml.v_3_1_1.DirectPositionType;
import net.opengis.xls.v_1_2_0.AbstractBodyType;
import net.opengis.xls.v_1_2_0.AbstractResponseParametersType;
import net.opengis.xls.v_1_2_0.DetermineRouteResponseType;
import net.opengis.xls.v_1_2_0.ResponseType;
import net.opengis.xls.v_1_2_0.RouteGeometryType;
import net.opengis.xls.v_1_2_0.XLSType;

import org.gofleet.openLS.OpenLS;
import org.gofleet.openLS.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class RoutingServiceTests {

	@Autowired
	OpenLS openLS;

	@Test
	public void testSimpleRoute() throws FileNotFoundException, JAXBException,
			XMLStreamException, FactoryConfigurationError, SAXException {
		XLSType object = openLS.openLS(
				Utils.convertFile2XLSType("/determineRouteRequest.xml",
						XLSType.class)).getValue();

		assertNotNull("Empty response", object);

		assertTrue("This is no XLS object", object instanceof XLSType);

		XLSType xls = (XLSType) object;

		assertNotNull("The response is null", xls);

		assertNotNull("The body is null.", xls.getBody());

		List<JAXBElement<? extends AbstractBodyType>> body = xls.getBody();

		assertNotNull("The body is null! How? We have just checked it!", body);

		assertEquals("The body should be unique", body.size(), 1);

		Object o = body.get(0).getValue();

		assertTrue("This is no response!", o instanceof ResponseType);

		ResponseType response = (ResponseType) o;

		assertNotNull("The contents of the body are null? (ResponseType)",
				response);

		assertEquals("I should have only one response", response
				.getNumberOfResponses().intValue(), 1);

		assertNotNull("Response parameters are null!",
				response.getResponseParameters());

		AbstractResponseParametersType arpt = response.getResponseParameters()
				.getValue();

		assertTrue("The response is not a route response",
				arpt instanceof DetermineRouteResponseType);

		DetermineRouteResponseType drrt = (DetermineRouteResponseType) arpt;

		assertNotNull("There should be a response (determineRouteResponse",
				drrt);

		assertNotNull("The geometry shouldn't be null", drrt.getRouteGeometry());

		RouteGeometryType routeGeometry = drrt.getRouteGeometry();

		assertNotNull("There should be a linestring",
				routeGeometry.getLineString());

		List<JAXBElement<?>> posOrPointPropertyOrPointRep = routeGeometry
				.getLineString().getPosOrPointPropertyOrPointRep();

		assertNotNull("There should be a list of positions",
				posOrPointPropertyOrPointRep);

		assertEquals("I was expecting five points",
				posOrPointPropertyOrPointRep.size(), 10);

		for (JAXBElement<?> element : posOrPointPropertyOrPointRep) {
			assertNotNull(element);
			o = element.getValue();
			assertNotNull(o);
			assertTrue(o instanceof DirectPositionType);
			DirectPositionType dpt = new DirectPositionType();
			if (dpt.getValue().size() > 0)
				assertEquals("Are we working on " + dpt.getValue().size()
						+ " dimensions?" + dpt, 2, dpt.getValue().size());

			for (Double d : dpt.getValue())
				assertNotNull(d);
		}

	}
}
