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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.openLS.tsp.TSPAlgorithm;
import org.gofleet.openLS.tsp.TSPStop;
import org.gofleet.openLS.tsp.TSPStopBag;

public class BackTrackingTSP implements TSPAlgorithm {
	private static Log LOG = LogFactory.getLog(BackTrackingTSP.class);

	private Boolean partialSolution = false;
	private Integer seconds = 15;

	public BackTrackingTSP() {
	}

	/**
	 * If we cannot find a solution, do you like to get the best partial
	 * solution reached?
	 * 
	 * @param partialSolution
	 */
	public BackTrackingTSP(Boolean partialSolution, Integer seconds) {
		this();
		if (partialSolution != null)
			this.partialSolution = partialSolution;
		this.seconds = seconds;
	}

	public List<TSPStop> order(TSPStopBag _bag) {

		DistanceMatrix distances = new DistanceMatrix();

		initializeMatrix(distances, _bag);

		Runtime runtime = Runtime.getRuntime();
		int numthreads = runtime.availableProcessors() * 3;

		ExecutorService executor = Executors.newFixedThreadPool(numthreads);

		List<BackTrackSolution> solutions = Collections
				.synchronizedList(new LinkedList<BackTrackSolution>());

		executor.execute(new Backtracking(_bag, distances, solutions));

		executor.shutdown();
		try {
			executor.awaitTermination(this.seconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			if (!this.partialSolution) {
				throw new RuntimeException(
						"Timeout reached. I couldn't find a solution on a proper time. "
								+ "Please, give me another chance with more time or"
								+ " accept a partial solution. I won't fail you, I promise.",
						e);
			}
		}

		if (solutions.size() > 0)
			return getBest(solutions, distances, _bag.size());

		throw new RuntimeException(
				"Something went wrong. I couldn't find a solution."
						+ "Have you tried giving me more time and "
						+ "allowing me to produce partial solutions?");
	}

	private List<TSPStop> getBest(List<BackTrackSolution> solutions,
			DistanceMatrix distances, Integer size) {

		List<TSPStop> res = null;
		Double cost = Double.MAX_VALUE;

		for (BackTrackSolution sol : solutions) {
			if (sol.getDistance(distances) <= cost) {
				if (size == sol.getStack().size()) {
					cost = sol.getDistance(distances);
					res = sol.getStack();
				}
			}
		}
		if (res == null)
			throw new RuntimeException(
					"I'm embarrased, I was unable to find a solution for you. "
							+ "Please, forgive me. I am just a machine.");
		return res;

	}

	/**
	 * Initialices the distance matrix on background while tsp is running.
	 * 
	 * @param distances
	 * @param bag
	 */
	@SuppressWarnings("unchecked")
	private void initializeMatrix(DistanceMatrix distances, TSPStopBag bag) {

		Runtime runtime = Runtime.getRuntime();
		int numthreads = runtime.availableProcessors() * 3;

		ExecutorService executor = Executors.newFixedThreadPool(numthreads);

		List<BacktrackStop> candidates = new ArrayList<BacktrackStop>();
		candidates.addAll((Collection<? extends BacktrackStop>) bag.getAll());

		for (BacktrackStop from : candidates) {
			executor.execute(new InitializeDistances(from, candidates,
					distances));
		}
		executor.shutdown();
		try {
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.error(e, e);
		}
	}
}
