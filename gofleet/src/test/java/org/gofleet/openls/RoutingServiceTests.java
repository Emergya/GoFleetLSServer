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

import net.opengis.gml.v_3_1_1.DirectPositionListType;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.gofleet.openLS.OpenLS;
import org.gofleet.openLS.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.ogc.AbstractBodyType;
import org.jvnet.ogc.AbstractResponseParametersType;
import org.jvnet.ogc.DetermineRouteResponseType;
import org.jvnet.ogc.DirectPositionType;
import org.jvnet.ogc.ResponseType;
import org.jvnet.ogc.RouteGeometryType;
import org.jvnet.ogc.XLSType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class RoutingServiceTests {

	@Autowired
	OpenLS openLS;

	@Test
	public void testSimpleRoute() throws FileNotFoundException, JAXBException,
			XMLStreamException, FactoryConfigurationError, SAXException {
		JAXBElement<XLSType> convertFile2XLSType = Utils.convertFile2XLSType(
				"/determineRouteRequest.xml", XLSType.class);
		XLSType object = openLS.openLS(convertFile2XLSType);

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
		

		List<DirectPositionType> posOrPointPropertyOrPointRep = routeGeometry
				.getLineString().getPos();

		assertNotNull("There should be a list of positions",
				posOrPointPropertyOrPointRep);

		for (DirectPositionType element : posOrPointPropertyOrPointRep) {
			assertNotNull(element);
			o = element.getValue();
			assertNotNull(o);
			assertTrue(o instanceof DirectPositionListType);
			DirectPositionListType dpt = (DirectPositionListType) o;
			if (dpt.getValue().size() > 0)
				assertEquals("Are we working on " + dpt.getValue().size()
						+ " dimensions?" + dpt, 2, dpt.getValue().size());

			for (Double d : dpt.getValue())
				assertNotNull(d);
		}

	}

	@Test
	public void testSRSRouted() throws FileNotFoundException, JAXBException,
			XMLStreamException, FactoryConfigurationError, SAXException {
		JAXBElement<XLSType> convertFile2XLSType = Utils.convertFile2XLSType(
				"/determineRouteRequestSRS.xml", XLSType.class);
		assertNotNull(openLS.openLS(convertFile2XLSType));
	}

	@Test
	public void testSRS() throws FileNotFoundException, JAXBException,
			XMLStreamException, FactoryConfigurationError, SAXException,
			MismatchedDimensionException, TransformException, FactoryException {

		GeometryFactory gf = new GeometryFactory();

		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:23030");
		CRS.findMathTransform(sourceCRS, targetCRS);

		Double y = -3.7091297753788;
		Double x = 40.400858925754;

		Point p = gf.createPoint(new Coordinate(x, y));
		System.out.println(p);

		p = JTS.transform(p, CRS.findMathTransform(sourceCRS, targetCRS))
				.getCentroid();
		System.out.println(p);
		p = JTS.transform(p, CRS.findMathTransform(targetCRS, sourceCRS))
				.getCentroid();
		
		System.out.println(p);
	}
}
