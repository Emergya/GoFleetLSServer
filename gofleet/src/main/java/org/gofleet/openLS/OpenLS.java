package org.gofleet.openLS;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
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
import org.gofleet.openLS.handlers.GeocodingHandler;
import org.gofleet.openLS.handlers.RoutingHandler;
import org.gofleet.openLS.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
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
 * This software is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * As a special exception, if you link this library with other files to produce an executable, this library does not by itself cause
 * the resulting executable to be covered by the GNU General Public License. This exception does not however invalidate any other
 * reasons why the executable file might be covered by the GNU General Public License.
 */
@Controller(value = "openLSService")
@Scope("session")
@Path("/")
public class OpenLS {

    static Log LOG = LogFactory.getLog(OpenLS.class);

    @Autowired(required = false)
    RoutingHandler routingHandler;

    @Autowired(required = false)
    GeocodingHandler geocodingHandler;

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
     * Main function for the webservice. It determines the operation with the request parameters of {@link XLSType}
     *
     * @param parameter
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML,
        MediaType.APPLICATION_ATOM_XML})
    public XLSType openLS(JAXBElement<XLSType> jaxbelement) {
        try {
            final XLSType parameter = jaxbelement.getValue();
            LOG.trace("openLS(" + parameter + ")");
            Locale localetmp = Locale.ROOT;

            if (parameter.getLang() != null && !parameter.getLang().isEmpty()) {
                LOG.trace("Language detected: " + parameter.getLang());
                localetmp = new Locale(parameter.getLang());
            }
            final Locale locale = localetmp;
            localetmp = null;
            final List<List<AbstractResponseParametersType>> resultado = new LinkedList<List<AbstractResponseParametersType>>();

            ExecutorService executor = Executors.newFixedThreadPool(3);

            for (JAXBElement<? extends AbstractBodyType> jaxbbody : parameter.getBody()) {

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
                                        if (request instanceof DetermineRouteRequestType) {
                                            response = routePlan(
                                                    (DetermineRouteRequestType) request,
                                                    locale);
                                        } else if (request instanceof ReverseGeocodeRequestType) {
                                            response = reverseGeocoding((ReverseGeocodeRequestType) request);
                                        } else if (request instanceof GeocodeRequestType) {
                                            response = geocoding((GeocodeRequestType) request);
                                        } else if (request instanceof DirectoryRequestType) {
                                            response = directory((DirectoryRequestType) request);
                                        }

                                        synchronized (resultado) {
                                            resultado.add(response);
                                        }
                                    } catch (Throwable e) {
                                        LOG.error("Error answering request", e);
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
            return Utils.envelop(resultado, locale).getValue();
        } catch (Throwable t) {
            LOG.error("Unexpected error. Help!", t);

            return null;
        }

    }

    /**
     * Calls the routing method
     *
     * @param epsg
     *
     * @param parameter
     * @return
     */
    protected List<AbstractResponseParametersType> routePlan(
            DetermineRouteRequestType param, Locale locale) {

        List<AbstractResponseParametersType> list = new LinkedList<AbstractResponseParametersType>();
        AbstractResponseParametersType arpt = null;

        if (routingHandler == null) {
            throw new IllegalArgumentException("Route requests not supported by the server.");
        }

        try {
            arpt = routingHandler.routePlan(param);
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

        if (geocodingHandler == null) {
            throw new IllegalArgumentException("ReverseGeocoding requests not supported by the server.");
        }

        return geocodingHandler.reverseGeocode(request);
    }

    /**
     * Calls the directory method
     *
     * @param parameter
     * @return
     */
    protected List<AbstractResponseParametersType> directory(
            DirectoryRequestType param) {

        if (geocodingHandler == null) {
            throw new IllegalArgumentException("ReverseGeocoding requests not supported by the server.");
        }
        return geocodingHandler.directory(param);
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
        if (geocodingHandler == null) {
            throw new IllegalArgumentException("ReverseGeocoding requests not supported by the server.");
        }
        return geocodingHandler.geocoding(request);
    }

}
