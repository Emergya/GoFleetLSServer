package org.gofleet.openls;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import net.opengis.xls.v_1_2_0.XLSType;

import org.gofleet.openLS.OpenLS;
import org.gofleet.openLS.util.Utils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class SimpleServiceTests {

	@Autowired
	OpenLS openLS;
	static JAXBElement<XLSType> determineRouteRequest = null;
	static JAXBElement<XLSType> geocodingRequest = null;
	static JAXBElement<XLSType> reverseGeocodingRequest = null;
	static JAXBElement<XLSType> directoryRequest = null;

	@BeforeClass
	public static void initialize() throws FileNotFoundException, JAXBException {
		determineRouteRequest = Utils.convertFile2XLSType(
				"/determineRouteRequest.xml", XLSType.class);
		geocodingRequest = Utils.convertFile2XLSType("/geocodingRequest.xml",
				XLSType.class);
		reverseGeocodingRequest = Utils.convertFile2XLSType(
				"/reverseGeocoding.xml", XLSType.class);
		directoryRequest = Utils.convertFile2XLSType("/directory.xml",
				XLSType.class);
	}

	@Test(timeout = 1000)
	public void testEmptyRoute() throws FileNotFoundException, JAXBException {
		openLS.openLS(determineRouteRequest);
	}

	@Test(timeout = 1000)
	public void testEmptyGeocoding() throws FileNotFoundException,
			JAXBException {
		openLS.openLS(geocodingRequest);
	}
	
	@Test(timeout = 1000)
	public void testEmptyReverseGeocoding() throws FileNotFoundException,
			JAXBException {
		openLS.openLS(reverseGeocodingRequest);
	}
}
