package org.gofleet.openLS.util;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import net.opengis.xls.v_1_2_0.AbstractResponseParametersType;
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
			List<List<AbstractResponseParametersType>> params, Locale locale) {
		XLSType xlsType = new XLSType();
		xlsType.setVersion(BigDecimal.valueOf(1.2d));

		ResponseType responseType = new ResponseType();

		for (List<AbstractResponseParametersType> element : params) {
			if (element != null)
				for (AbstractResponseParametersType e : element) {
					String responseClass = e.getClass().getSimpleName()
							.toString();
					responseClass = responseClass.substring(0,
							responseClass.length() - 4);

					JAXBElement<? extends AbstractResponseParametersType> body_ = new JAXBElement(
							new QName("http://www.opengis.net/xls",
									responseClass, "xls"), e.getClass(), e);
					responseType.setResponseParameters(body_);
				}
			responseType.setNumberOfResponses(new BigInteger((new Integer(
					element.size())).toString()));
			responseType.setRequestID("-1");
			responseType.setVersion("0.9");
			xlsType.getBody()
					.add(new JAXBElement(new QName(
							"http://www.opengis.net/xls", "Response", "xls"),
							responseType.getClass(), responseType));
		}

		ResponseHeaderType header = new ResponseHeaderType();
		header.setSessionID("none");

		xlsType.setHeader(new JAXBElement<ResponseHeaderType>(new QName(
				"http://www.opengis.net/xls", "ResponseHeader", "xls"),
				ResponseHeaderType.class, header));
		
		xlsType.setLang(locale.getLanguage());

		JAXBElement<XLSType> res = new JAXBElement<XLSType>(new QName(
				"http://www.opengis.net/xls", "xls", "xls"), XLSType.class,
				xlsType);
		return res;
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

}
