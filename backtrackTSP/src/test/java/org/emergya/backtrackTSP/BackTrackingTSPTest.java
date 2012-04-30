/**
 * Copyright (C) 2012, Emergya (http://www.emergya.es)
 * 
 * @author <a href="mailto:marias@emergya.com">María Arias de Reyna</a>
 * 
 *         This file is part of GoFleet
 * 
 *         This software is free software; you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation; either version 2 of the License, or (at
 *         your option) any later version.
 * 
 *         This software is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 * 
 *         You should have received a copy of the GNU General Public License
 *         along with this library; if not, write to the Free Software
 *         Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *         02110-1301 USA
 * 
 *         As a special exception, if you link this library with other files to
 *         produce an executable, this library does not by itself cause the
 *         resulting executable to be covered by the GNU General Public License.
 *         This exception does not however invalidate any other reasons why the
 *         executable file might be covered by the GNU General Public License.
 */
package org.emergya.backtrackTSP;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.gofleet.openLS.tsp.TSPStop;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class BackTrackingTSPTest {

	private GeometryFactory gf = new GeometryFactory();

	@Test
	public void simpleTest() {
		BackTrackingTSP backtracking = new BackTrackingTSP();
		List<TSPStop> stops = new LinkedList<TSPStop>();

		stops.add(new BacktrackStop(1, gf.createPoint(new Coordinate(
				-3.7091297753d, 40.40085892d))));
		stops.add(new BacktrackStop(2, gf.createPoint(new Coordinate(
				-3.7234753d, 40.401237892d))));
		stops.add(new BacktrackStop(3, gf.createPoint(new Coordinate(
				-3.724762553d, 40.40543562d))));
		stops.add(new BacktrackStop(4, gf.createPoint(new Coordinate(
				-3.7578463d, 40.40462346252d))));
		stops.add(new BacktrackStop(5, gf.createPoint(new Coordinate(
				-3.726525635453d, 40.422222456252d))));
		stops.add(new BacktrackStop(6, gf.createPoint(new Coordinate(
				-3.722566553d, 40.425624492d))));
		stops.add(new BacktrackStop(7, gf.createPoint(new Coordinate(
				-3.7223562453d, 40.40434567456692d))));
		stops.add(new BacktrackStop(9, gf.createPoint(new Coordinate(
				-3.722362543d, 40.40262352d))));
		stops.add(new BacktrackStop(10, gf.createPoint(new Coordinate(
				-3.724567456753d, 40.402345234592d))));

		BacktrackStopBag bag = new BacktrackStopBag(stops);

		long time = System.currentTimeMillis();
		final List<TSPStop> order = backtracking.order(bag);
		System.out.println(System.currentTimeMillis() - time + "ms");
		assertNotNull(order);
		System.out.println(order);
	}

	@Test
	public void firstLastTest() {
		BackTrackingTSP backtracking = new BackTrackingTSP();
		List<TSPStop> stops = new LinkedList<TSPStop>();

		stops.add(new BacktrackStop(1, gf.createPoint(new Coordinate(
				-3.7091297753d, 40.40085892d))));
		stops.add(new BacktrackStop(2, gf.createPoint(new Coordinate(
				-3.7234753d, 40.401237892d))));
		stops.add(new BacktrackStop(3, gf.createPoint(new Coordinate(
				-3.724762553d, 40.40543562d))));
		stops.add(new BacktrackStop(5, gf.createPoint(new Coordinate(
				-3.726525635453d, 40.422222456252d))));
		stops.add(new BacktrackStop(6, gf.createPoint(new Coordinate(
				-3.722566553d, 40.425624492d))));
		stops.add(new BacktrackStop(7, gf.createPoint(new Coordinate(
				-3.7223562453d, 40.40434567456692d))));
		stops.add(new BacktrackStop(8, gf.createPoint(new Coordinate(
				-3.722362543d, 40.40262352d))));

		BacktrackStopBag bag = new BacktrackStopBag(stops, new BacktrackStop(9,
				gf.createPoint(new Coordinate(-3.724567456753d,
						40.402345234592d))), new BacktrackStop(4,
				gf.createPoint(new Coordinate(-3.7578463d, 40.40462346252d))));

		long time = System.currentTimeMillis();
		final List<TSPStop> order = backtracking.order(bag);
		System.out.println(System.currentTimeMillis() - time + "ms");
		assertNotNull(order);
		System.out.println(order);
	}

	@Test
	public void performance() {

		Random r = new Random();
		Double x = -4.6d;
		Double y = 37.5d;

		int max = 4;
		int numparadas = 20;

		long totaltime = 0;

		for (int k = 3; k < numparadas; k++) {

			for (int i = 0; i < max; i++) {

				BackTrackingTSP backtracking = new BackTrackingTSP();
				List<TSPStop> stops = new LinkedList<TSPStop>();

				for (int j = 0; j < k; j++)
					stops.add(new BacktrackStop(j, gf
							.createPoint(new Coordinate(x
									+ (r.nextFloat() * ((r.nextBoolean()) ? -1
											: 1)), y
									+ (r.nextFloat() * ((r.nextBoolean()) ? -1
											: 1))))));

				BacktrackStopBag bag = new BacktrackStopBag(stops);

				long time = System.currentTimeMillis();
				final List<TSPStop> order = backtracking.order(bag);
				totaltime += System.currentTimeMillis() - time;
				assertNotNull(order);
				assertTrue(order.size() == k);
				// System.out.println(order);
			}
			System.out.println("Time for " + k + " stops: " + (totaltime / max)
					+ "ms");
		}
	}

	@Test
	public void performanceWithLast() {

		Random r = new Random();
		Double x = -4.6d;
		Double y = 37.5d;

		int max = 4;
		int numparadas = 20;

		for (int k = 3; k < numparadas; k++) {

			long totaltime = 0;

			for (int i = 0; i < max; i++) {

				BackTrackingTSP backtracking = new BackTrackingTSP();
				List<TSPStop> stops = new LinkedList<TSPStop>();

				for (int j = 0; j < k - 2; j++)
					stops.add(new BacktrackStop(j, gf
							.createPoint(new Coordinate(x
									+ (r.nextFloat() * ((r.nextBoolean()) ? -1
											: 1)), y
									+ (r.nextFloat() * ((r.nextBoolean()) ? -1
											: 1))))));

				BacktrackStopBag bag = new BacktrackStopBag(stops,
						new BacktrackStop(k - 1,
								gf.createPoint(new Coordinate(
										x
												+ (r.nextFloat() * ((r
														.nextBoolean()) ? -1
														: 1)), y
												+ (r.nextFloat() * ((r
														.nextBoolean()) ? -1
														: 1))))),
						new BacktrackStop(k,
								gf.createPoint(new Coordinate(
										x
												+ (r.nextFloat() * ((r
														.nextBoolean()) ? -1
														: 1)), y
												+ (r.nextFloat() * ((r
														.nextBoolean()) ? -1
														: 1))))));

				long time = System.currentTimeMillis();
				final List<TSPStop> order = backtracking.order(bag);
				totaltime += System.currentTimeMillis() - time;
				assertNotNull(order);
				assertTrue(order.size() == k);
			}
			System.out.println("Time for " + k + " stops: " + (totaltime / max)
					+ "ms");
		}
	}
}
