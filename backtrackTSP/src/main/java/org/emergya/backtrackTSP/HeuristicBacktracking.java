package org.emergya.backtrackTSP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.openLS.tsp.TSPStop;
import org.gofleet.openLS.tsp.TSPStopBag;

import com.vividsolutions.jts.geom.Point;

/**
 * Like Backtracking but a simple heuristic which order the candidates to try
 * 
 * @author marias
 * 
 */
class HeuristicBacktracking extends Thread {
	private static Log LOG = LogFactory.getLog(HeuristicBacktracking.class);

	private BackTrackSolution current;
	private BacktrackStopBag bag;
	private DistanceMatrix distances;
	private SolutionContainer best;

	public HeuristicBacktracking(TSPStopBag _bag, DistanceMatrix distances,
			SolutionContainer best) {
		this.current = initializeResBag(_bag);
		this.bag = getBacktrackingBag(_bag);
		this.distances = distances;
		this.best = best;
	}

	@Override
	public void run() {
		try {
			BackTrackSolution solution = heuristicBacktracking(this.current,
					this.bag, this.distances, new BackTrackSolution(
							new Stack<TSPStop>()));
			best.add(solution);
		} catch (Throwable t) {
			LOG.error("Failure on heuristic backtracking procedure.", t);
		}
	}

	private BackTrackSolution heuristicBacktracking(BackTrackSolution current,
			BacktrackStopBag bag, DistanceMatrix distances,
			BackTrackSolution partialSolution) {

		Collection<? super TSPStop> all = bag.getAll();
		if (all.size() > 0) {

			List<BacktrackStop> candidates = null;
			candidates = new ArrayList<BacktrackStop>();
			for (Object s : all)
				candidates.add((BacktrackStop) s);

			BacktrackStop from = null;
			if (current.getStack().size() > 0)
				from = (BacktrackStop) current.getStack().peek();
			BacktrackStop end = (BacktrackStop) bag.getLast();
			HeuristicComparator heuristicComparator = new HeuristicComparator(
					distances, from, end);
			Collections.sort(candidates, heuristicComparator);

			for (TSPStop next : candidates) {
				bag.removeStop(next);
				current.push(next);

				// If we already have a solution candidate, is the current way
				// worse than it? If not, just use current solution as better
				if (!(partialSolution != null && current.getDistance(distances) > partialSolution
						.getDistance(distances))) {
					heuristicBacktracking(current, bag, distances,
							partialSolution);
				}
				if (LOG.isTraceEnabled())
					LOG.trace("Current: " + current);

				current.pop();
				bag.addStop(next);
			}

		} else {
			current.push(bag.getLast());

			// Maybe another thread found a better solution
			partialSolution = checkIfBetterAnotherThread(distances,
					partialSolution);

			if (current.getDistance(distances) < partialSolution
					.getDistance(distances)) {
				partialSolution.setStack(current.getStack());
				this.best.add(partialSolution);
			}

			current.pop();
		}

		partialSolution.getDistance(distances);
		return partialSolution;
	}

	private BackTrackSolution checkIfBetterAnotherThread(
			DistanceMatrix distances, BackTrackSolution partialSolution) {

		// Maybe another thread found a better solution
		if (partialSolution != null && this.best.getSolution() != null) {
			if (partialSolution.getDistance(distances) < this.best
					.getSolution().getDistance(distances))
				partialSolution = (BackTrackSolution) this.best.getSolution()
						.clone();

		}
		return partialSolution;
	}

	private BackTrackSolution initializeResBag(TSPStopBag bag) {
		BackTrackSolution res = new BackTrackSolution(new Stack<TSPStop>());

		if (bag.hasFirst()) {
			res.push(bag.getFirst());
		}
		return res;
	}

	private BacktrackStopBag getBacktrackingBag(TSPStopBag _bag) {
		List<TSPStop> all = new LinkedList<TSPStop>();
		all.addAll((Collection<? extends TSPStop>) _bag.getAll());
		return new BacktrackStopBag(all, _bag.getFirst(), _bag.getLast());
	}

}

class HeuristicComparator implements Comparator<BacktrackStop> {
	private BacktrackStop end = null;
	private DistanceMatrix distances = null;
	private BacktrackStop from = null;

	protected HeuristicComparator(DistanceMatrix distances, BacktrackStop from,
			BacktrackStop end) {
		this.end = end;
		this.distances = distances;
		this.from = from;
	}

	public int compare(BacktrackStop o1, BacktrackStop o2) {
		if (from != null) {
			Double dist1 = (distances.distance(from, o1) + heuristic(o1, end));
			Double dist2 = (distances.distance(from, o2) + heuristic(o2, end));
			return dist1.compareTo(dist2);
		} else {
			Double dist1 = heuristic(o1, end);
			Double dist2 = heuristic(o2, end);
			return dist1.compareTo(dist2);
		}
	}

	private Double heuristic(BacktrackStop from, BacktrackStop to) {
		Point a = from.getPosition();
		Point b = to.getPosition();

		return a.distance(b);
	}

}