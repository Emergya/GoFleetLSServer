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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.openLS.tsp.TSPAlgorithm;
import org.gofleet.openLS.tsp.TSPStop;
import org.gofleet.openLS.tsp.TSPStopBag;

public class BackTrackingTSP implements TSPAlgorithm {
	private static Log LOG = LogFactory.getLog(BackTrackingTSP.class);

	private Boolean partialSolution = false;
	private Integer seconds = 18;

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
		long time = System.currentTimeMillis();

		DistanceMatrix distances = new DistanceMatrix();

		initializeMatrix(distances, _bag);

		Runtime runtime = Runtime.getRuntime();
		int numthreads = runtime.availableProcessors() * 10;

		final ExecutorService executor = Executors
				.newFixedThreadPool(numthreads);

		SolutionContainer solutions = new SolutionContainer(distances);

		if (_bag.size() > 7) {
			if (_bag.hasLast()) {
				run(executor, new AStar(_bag, distances, solutions));
				run(executor, new HeuristicBacktracking(_bag, distances,
						solutions));
			} else {
				for (TSPStop stop : _bag.getAll()) {
					List<TSPStop> stops = new ArrayList<TSPStop>();
					stops.addAll(_bag.getAll());
					stops.remove(stop);

					BacktrackStopBag bag = new BacktrackStopBag(stops,
							_bag.getFirst(), stop);
					run(executor, new AStar(bag, distances, solutions));
					run(executor, new HeuristicBacktracking(bag, distances,
							solutions));
				}
			}
		}
		run(executor, new Backtracking(_bag, distances, solutions));

		executor.shutdown();

		try {
			if (!executor.awaitTermination(
					this.seconds - (System.currentTimeMillis() - time) / 1000,
					TimeUnit.SECONDS))
				executor.shutdownNow();
		} catch (InterruptedException e) {
			if (!this.partialSolution) {
				throw new RuntimeException(
						"Timeout reached. I couldn't find a solution on a proper time. "
								+ "Please, give me another chance with more time or"
								+ " accept a partial solution. I won't fail you, I promise.",
						e);
			}
		}

		return getBest(solutions, distances, _bag.size());
	}

	private void run(final ExecutorService executor, final Runnable aStar) {
		final FutureTask<Boolean> task = new FutureTask<Boolean>(aStar, true);
		executor.execute(task);
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					task.get(BackTrackingTSP.this.seconds, TimeUnit.SECONDS);
				} catch (Throwable e) {
				}
			}

		};
		t.start();
	}

	private List<TSPStop> getBest(SolutionContainer solutions,
			DistanceMatrix distances, Integer size) {

		BackTrackSolution solution = solutions.getSolution();

		if (solution == null)
			throw new RuntimeException(
					"I'm embarrased, I was unable to find a solution for you. "
							+ "Please, forgive me. I am just a machine.");

		return solution.getStack();

	}

	/**
	 * Initialices the distance matrix on background while tsp is running.
	 * 
	 * @param distances
	 * @param bag
	 */
	private void initializeMatrix(DistanceMatrix distances, TSPStopBag bag) {

		Runtime runtime = Runtime.getRuntime();
		int numthreads = runtime.availableProcessors() * 3;

		ExecutorService executor = Executors.newFixedThreadPool(numthreads);

		List<BacktrackStop> candidates = null;
		candidates = new ArrayList<BacktrackStop>();
		for (TSPStop stop : bag.getAll())
			candidates.add((BacktrackStop) stop);

		for (BacktrackStop from : candidates) {
			executor.execute(new InitializeDistances(from, candidates,
					distances));
		}
		executor.shutdown();
		try {
			executor.awaitTermination(6, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.error(e, e);
		}
	}
}

class SolutionContainer {
	private BackTrackSolution solution = null;
	private DistanceMatrix distances = null;

	public SolutionContainer(DistanceMatrix distances) {
		this.distances = distances;
	}

	public BackTrackSolution getSolution() {
		synchronized (this) {
			return this.solution;
		}
	}

	public void add(BackTrackSolution solution) {
		synchronized (this) {
			if (this.solution == null)
				this.solution = solution;
			else {
				if (this.solution.getDistance(distances) > solution
						.getDistance(distances))
					this.solution = solution;
			}
		}
	}
}
