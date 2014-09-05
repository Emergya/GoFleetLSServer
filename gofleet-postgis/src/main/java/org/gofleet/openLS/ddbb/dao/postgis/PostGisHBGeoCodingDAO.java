/*
 * Copyright (C) 2011, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:marias@emergya.com">María Arias</a>
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
package org.gofleet.openLS.ddbb.dao.postgis;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.opengis.xls.v_1_2_0.AbstractResponseParametersType;
import net.opengis.xls.v_1_2_0.AddressType;
import net.opengis.xls.v_1_2_0.DirectoryRequestType;
import net.opengis.xls.v_1_2_0.GeocodeRequestType;
import net.opengis.xls.v_1_2_0.GeocodeResponseListType;
import net.opengis.xls.v_1_2_0.GeocodeResponseType;
import net.opengis.xls.v_1_2_0.GeocodedAddressType;
import net.opengis.xls.v_1_2_0.NamedPlaceClassification;
import net.opengis.xls.v_1_2_0.NamedPlaceType;
import net.opengis.xls.v_1_2_0.PositionType;
import net.opengis.xls.v_1_2_0.ReverseGeocodeRequestType;
import net.opengis.xls.v_1_2_0.ReverseGeocodeResponseType;
import net.opengis.xls.v_1_2_0.ReverseGeocodedLocationType;
import net.opengis.xls.v_1_2_0.StreetAddressType;
import net.opengis.xls.v_1_2_0.StreetNameType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.openLS.util.GeoUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.postgis.PGgeometry;
import org.postgresql.jdbc4.Jdbc4Array;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Geometry;
import org.gofleet.openLS.ddbb.dao.HibernateDAOBase;
import org.gofleet.openLS.ddbb.utils.PostGisUtils;

@Repository
public class PostGisHBGeoCodingDAO extends HibernateDAOBase {

	@SuppressWarnings("unused")
	private static final String EPSG_4326 = "EPSG:4326";


	public static Log LOG = LogFactory.getLog(PostGisHBGeoCodingDAO.class);


	@Transactional(readOnly = true)
	public List<AbstractResponseParametersType> reverseGeocode(
			final ReverseGeocodeRequestType param) {
		HibernateCallback<List<AbstractResponseParametersType>> action = new HibernateCallback<List<AbstractResponseParametersType>>() {
			public List<AbstractResponseParametersType> doInHibernate(
					Session session) throws HibernateException, SQLException {
				PositionType position = param.getPosition();

				position.getPoint().getPos().getValue();

				Geometry geometry = GeoUtil.getGeometry(position);

				List<AbstractResponseParametersType> res_ = new LinkedList<AbstractResponseParametersType>();

				// TODO change deprecation?
				@SuppressWarnings("deprecation")
				CallableStatement consulta = session.connection().prepareCall(
						"{call gls_reverse_geocoding(?)}");
				PGgeometry geom = new PGgeometry(geometry.toText());
				consulta.setObject(1, geom);

				LOG.debug(consulta);

				ResultSet o = consulta.executeQuery();
				ReverseGeocodeResponseType grt = new ReverseGeocodeResponseType();
				while (o.next()) {
					ReverseGeocodedLocationType geocode = new ReverseGeocodedLocationType();
					if (geocode.getAddress() == null)
						geocode.setAddress(new AddressType());
					if (geocode.getAddress().getStreetAddress() == null)
						geocode.getAddress().setStreetAddress(
								new StreetAddressType());
					for (int i = 1; i < o.getMetaData().getColumnCount(); i++) {
						String value = new String(o.getString(i).getBytes(),
								Charset.forName("ISO-8859-1"));
						if (o.getMetaData().getColumnName(i).equals("street")) {
							StreetNameType street = new StreetNameType();
							street.setValue(value);
							street.setOfficialName(value);
							geocode.getAddress().getStreetAddress().getStreet()
									.add(street);
						} else if (o.getMetaData().getColumnName(i)
								.equals("munsub")) {
							NamedPlaceType place = new NamedPlaceType();
							place.setValue(value);
							place.setType(NamedPlaceClassification.MUNICIPALITY_SUBDIVISION);
							geocode.getAddress().getPlace().add(place);
						} else if (o.getMetaData().getColumnName(i)
								.equals("mun")) {
							NamedPlaceType place = new NamedPlaceType();
							place.setValue(value);
							place.setType(NamedPlaceClassification.MUNICIPALITY);
							geocode.getAddress().getPlace().add(place);
						} else if (o.getMetaData().getColumnName(i)
								.equals("subcountry")) {
							NamedPlaceType place = new NamedPlaceType();
							place.setValue(value);
							place.setType(NamedPlaceClassification.COUNTRY_SUBDIVISION);
							geocode.getAddress().getPlace().add(place);
						} else if (o.getMetaData().getColumnName(i)
								.equals("country")) {
							geocode.getAddress().setCountryCode(value);
						}
					}
					try {

						grt.getReverseGeocodedLocation().add(geocode);
					} catch (Throwable t) {
						LOG.error("Error extracting data from database.", t);
					}
				}
				res_.add(grt);
				return res_;
			}

		};

		return hibernateTemplate.executeWithNativeSession(action);
	}

	@Transactional(readOnly = true)
	public List<AbstractResponseParametersType> directory(
			DirectoryRequestType param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transactional(readOnly = true)
	public List<AbstractResponseParametersType> geocoding(
			final GeocodeRequestType param) {
		HibernateCallback<List<AbstractResponseParametersType>> action = new HibernateCallback<List<AbstractResponseParametersType>>() {
			public List<AbstractResponseParametersType> doInHibernate(
					Session session) throws HibernateException, SQLException {

				List<AddressType> addressList = param.getAddress();
                List<AbstractResponseParametersType> res_ = new LinkedList<AbstractResponseParametersType>();

				for (AddressType addressType : addressList) {

					// TODO change deprecation?
					@SuppressWarnings("deprecation")
					CallableStatement consulta = session.connection()
							.prepareCall("{call gls_geocoding(?, ?, ?, ?, ?)}");

					String street = GeoUtil.extractStreet(addressType);
					String munsub = GeoUtil.extractMunSub(addressType);
					String mun = GeoUtil.extractMun(addressType);
					String subcountry = GeoUtil.extractSubCountry(addressType);
					String country = GeoUtil.extractCountry(addressType);

					consulta.setString(1, street);
					consulta.setString(2, munsub);
					consulta.setString(3, mun);
					consulta.setString(4, subcountry);
					consulta.setString(5, country);

					LOG.debug(consulta);

					ResultSet o = consulta.executeQuery();
					GeocodeResponseType grt = new GeocodeResponseType();
					while (o.next()) {
						GeocodeResponseListType geocode = new GeocodeResponseListType();
						try {
							PGgeometry g = (PGgeometry) o.getObject("geometry");
							Jdbc4Array address = (Jdbc4Array) o
									.getArray("address");

							GeocodedAddressType addresstype = new GeocodedAddressType();
							addresstype.setPoint(PostGisUtils.getReferencedPoint(g));
							addresstype.setAddress(PostGisUtils.getAddress(address));

							geocode.getGeocodedAddress().add(addresstype);

							geocode.setNumberOfGeocodedAddresses(BigInteger
									.valueOf(1l));

							grt.getGeocodeResponseList().add(geocode);
						} catch (Throwable t) {
							LOG.error("Error extracting data from database.", t);
						}
						res_.add(grt);
					}
				}
				return res_;
			}

		};

		return hibernateTemplate.executeWithNativeSession(action);
	}
}
