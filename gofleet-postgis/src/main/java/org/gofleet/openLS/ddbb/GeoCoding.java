package org.gofleet.openLS.ddbb;

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
import java.util.List;

import javax.annotation.Resource;

import net.opengis.xls.v_1_2_0.AbstractResponseParametersType;
import net.opengis.xls.v_1_2_0.DirectoryRequestType;
import net.opengis.xls.v_1_2_0.GeocodeRequestType;
import net.opengis.xls.v_1_2_0.ReverseGeocodeRequestType;

import org.apache.commons.lang3.StringUtils;
import org.gofleet.openLS.ddbb.dao.postgis.PostGisHBGeoCodingDAO;
import org.gofleet.openLS.handlers.GeocodingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeoCoding implements GeocodingHandler {

	@Autowired
	private PostGisHBGeoCodingDAO dao;

    @Override
	public List<AbstractResponseParametersType> directory(
			DirectoryRequestType param) {
		return dao.directory(param);
	}

    @Override
	public List<AbstractResponseParametersType> reverseGeocode(
			ReverseGeocodeRequestType param) {
		return dao.reverseGeocode(param);
	}

    @Override
	public List<AbstractResponseParametersType> geocoding(
			GeocodeRequestType param) {
		return dao.geocoding(param);
	}

	/**
	 * Check if rules contains method, ignoring case.
	 * 
	 * @param rules
	 * @param method
	 * @return
	 */
	public boolean equals(String[] rules, String method) {
		for (String rule : rules)
			if (StringUtils.equalsIgnoreCase(method, rule))
				return true;
		return false;
	}

}
