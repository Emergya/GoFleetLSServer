package org.gofleet.openLS.util;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import net.opengis.xls.v_1_2_0.AbstractResponseParametersType;
import net.opengis.xls.v_1_2_0.RequestType;
import net.opengis.xls.v_1_2_0.ResponseHeaderType;
import net.opengis.xls.v_1_2_0.ResponseType;
import net.opengis.xls.v_1_2_0.XLSType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
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
public class Utils {
	static Log LOG = LogFactory.getLog(Utils.class);

	/**
	 * Envelops an {@link AbstractResponseParametersType} response inside the
	 * {@link XLSType}
	 * 
	 * @param element
	 * @return
	 * @throws AxisFault
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static JAXBElement<XLSType> envelop(
			List<List<AbstractResponseParametersType>> params) {
		XLSType xlsType = new XLSType();

		ResponseType responseType = new ResponseType();

		for (List<AbstractResponseParametersType> element : params) {
			for (AbstractResponseParametersType e : element) {
				String responseClass = e.getClass().getSimpleName().toString();
				responseClass = responseClass.substring(0,
						responseClass.length() - 4);

				JAXBElement<? extends AbstractResponseParametersType> body_ = new JAXBElement(
						new QName("http://www.opengis.net/xls", responseClass,
								""), e.getClass(), e);
				responseType.setResponseParameters(body_);
			}
			responseType.setNumberOfResponses(new BigInteger((new Integer(
					element.size())).toString()));
			xlsType.getBody()
					.add(new JAXBElement(new QName(
							"http://www.opengis.net/xls", "Response"),
							responseType.getClass(), responseType));
		}

		ResponseHeaderType header = new ResponseHeaderType();

		xlsType.setHeader(new JAXBElement<ResponseHeaderType>(new QName(
				"http://www.opengis.net/xls", "ResponseHeader"),
				ResponseHeaderType.class, header));

		JAXBElement<XLSType> res = new JAXBElement(new QName(
				"http://www.opengis.net/xls", "xls"), XLSType.class, xlsType);

		return res;
	}

	/**
	 * Given a {@link XLSType} object, it returns the method called.
	 * 
	 * @param parameter
	 * @return
	 * @throws AxisFault
	 */
	public static String getMethod(XLSType xlsType) {
		try {
			@SuppressWarnings("unchecked")
			RequestType requestType = ((JAXBElement<RequestType>) xlsType
					.getBody().get(0)).getValue();

			return requestType.getMethodName();
		} catch (Throwable t) {
			LOG.error("Error getting method.", t);
			throw new RuntimeException(t);
		}
	}

	/**
	 * Check if rules contains method, ignoring case.
	 * 
	 * @param rules
	 * @param method
	 * @return
	 */
	public static boolean equals(String[] rules, String method) {
		for (String rule : rules)
			if (StringUtils.equalsIgnoreCase(method, rule))
				return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	public static JAXBElement<XLSType> convertFile2XLSType(String path,
			Class<?> classType) throws FileNotFoundException, JAXBException {
		Unmarshaller m = JAXBContext.newInstance(classType)
				.createUnmarshaller();
		Object tmp = m.unmarshal(Utils.class.getResourceAsStream(path));

		return ((JAXBElement<XLSType>) tmp);
	}

	public static XLSType convertString2OMElement(String xml)
			throws JAXBException {
		Unmarshaller m = JAXBContext.newInstance(XLSType.class)
				.createUnmarshaller();
		StringReader sr = new StringReader(xml);
		XLSType tmp = (XLSType) m.unmarshal(sr);
		return tmp;
	}

}
