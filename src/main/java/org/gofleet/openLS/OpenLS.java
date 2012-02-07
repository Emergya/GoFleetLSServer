package org.gofleet.openLS;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import net.opengis.xls.v_1_2_0.AbstractBodyType;
import net.opengis.xls.v_1_2_0.AbstractRequestParametersType;
import net.opengis.xls.v_1_2_0.AbstractResponseParametersType;
import net.opengis.xls.v_1_2_0.DetermineRouteRequestType;
import net.opengis.xls.v_1_2_0.DirectoryRequestType;
import net.opengis.xls.v_1_2_0.GeocodeRequestType;
import net.opengis.xls.v_1_2_0.RequestType;
import net.opengis.xls.v_1_2_0.ReverseGeocodeRequestType;
import net.opengis.xls.v_1_2_0.XLSType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.referencing.CRS;
import org.gofleet.configuration.Configuration;
import org.gofleet.openLS.ddbb.GeoCoding;
import org.gofleet.openLS.ddbb.Routing;
import org.gofleet.openLS.util.MoNaVConnector;
import org.gofleet.openLS.util.OSRMConnector;
import org.gofleet.openLS.util.Utils;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.xml.sax.SAXException;

/**
 * Copyright (C) 2011, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:marias@emergya.es">María Arias</a>
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
@Controller(value = "openLSService")
@Scope("session")
@Path("/")
public class OpenLS {
	static Log LOG = LogFactory.getLog(OpenLS.class);

	@Resource
	private Routing routingController;

	@Resource
	private GeoCoding geoCodingController;

	private MoNaVConnector monavConnector = new MoNaVConnector();
	private OSRMConnector osrmConnector = new OSRMConnector();

	/**
	 * Stupid test to see if the Server is alive.
	 * 
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "Have you tried sending a XLS request by POST?";
	}

	/**
	 * Main function for the webservice. It determines the operation with the
	 * request parameters of {@link XLSType}
	 * 
	 * @param parameter
	 * @return
	 */
	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML,
			MediaType.APPLICATION_ATOM_XML })
	public JAXBElement<XLSType> openLS(JAXBElement<XLSType> jaxbelement) {
		final XLSType parameter = jaxbelement.getValue();
		LOG.trace("openLS(" + parameter + ")");
		final List<List<AbstractResponseParametersType>> resultado = new LinkedList<List<AbstractResponseParametersType>>();

		ExecutorService executor = Executors.newFixedThreadPool(3);

		for (JAXBElement<? extends AbstractBodyType> jaxbbody : parameter
				.getBody()) {

			AbstractBodyType body = jaxbbody.getValue();

			if (body instanceof RequestType) {

				final AbstractRequestParametersType request = ((RequestType) body)
						.getRequestParameters().getValue();

				FutureTask<List<AbstractResponseParametersType>> thread = new FutureTask<List<AbstractResponseParametersType>>(
						new Callable<List<AbstractResponseParametersType>>() {

							public List<AbstractResponseParametersType> call()
									throws Exception {
								List<AbstractResponseParametersType> response = null;
								try {
									if (request instanceof DetermineRouteRequestType)
										response = routePlan((DetermineRouteRequestType) request);
									else if (request instanceof ReverseGeocodeRequestType)
										response = reverseGeocoding((ReverseGeocodeRequestType) request);
									else if (request instanceof GeocodeRequestType)
										response = geocoding((GeocodeRequestType) request);
									else if (request instanceof DirectoryRequestType)
										response = directory((DirectoryRequestType) request);

									synchronized (resultado) {
										resultado.add(response);
									}
								} catch (Throwable e) {
									LOG.error(e, e);
									throw new RuntimeException(e);
								}
								return response;
							}
						});
				executor.execute(thread);
			}
		}

		executor.shutdown();

		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.error(e, e);
		}

		return Utils.envelop(resultado);
	}

	/**
	 * Calls the routing method
	 * @param epsg 
	 * 
	 * @param parameter
	 * @return
	 */
	protected List<AbstractResponseParametersType> routePlan(
			DetermineRouteRequestType param) {

		List<AbstractResponseParametersType> list = new LinkedList<AbstractResponseParametersType>();
		AbstractResponseParametersType arpt = null;
		try {
			String conn = Configuration.get("RoutingConnector", "default");
			if (conn.equals("PGROUTING"))
				arpt = routingController.routePlan(param);
			else if (conn.equals("MONAV"))
				arpt = monavConnector.routePlan(param);
			else
				arpt = osrmConnector.routePlan(param);
			
		} catch (Throwable t) {
			LOG.error("Error on routePlan", t);
		}
		list.add(arpt);
		return list;
	}

	/**
	 * Calls the reverseGeocoding method
	 * 
	 * @param parameter
	 * @return
	 */
	protected List<AbstractResponseParametersType> reverseGeocoding(
			ReverseGeocodeRequestType request) {
		return geoCodingController.reverseGeocode(request);
	}

	/**
	 * Calls the directory method
	 * 
	 * @param parameter
	 * @return
	 */
	protected List<AbstractResponseParametersType> directory(
			DirectoryRequestType param) {
		return geoCodingController.directory(param);
	}

	/**
	 * Calls the geocoding method
	 * 
	 * @param parameter
	 * @return
	 * @throws AxisFault
	 * @throws SAXException
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 * @throws JAXBException
	 */
	protected List<AbstractResponseParametersType> geocoding(
			GeocodeRequestType request) {
		return geoCodingController.geocoding(request);
	}

}
