package org.emergya.backtrackTSP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.openLS.tsp.TSPStop;
import org.gofleet.openLS.tsp.TSPStopBag;

class Backtracking extends Thread {
	private static Log LOG = LogFactory.getLog(Backtracking.class);

	private BackTrackSolution current;
	private BacktrackStopBag bag;
	private DistanceMatrix distances;
	private SolutionContainer best;

	public Backtracking(TSPStopBag _bag, DistanceMatrix distances,
			SolutionContainer best) {
		this.current = initializeResBag(_bag);
		this.bag = getBacktrackingBag(_bag);
		this.distances = distances;
		this.best = best;
	}

	@Override
	public void run() {
		BackTrackSolution solution = backtrack(this.current, this.bag,
				this.distances, new BackTrackSolution(new Stack<TSPStop>()));
		this.best.add(solution);
	}

	/**
	 * Recursive main procedure
	 * 
	 * @param current
	 * @param bag
	 * @param distances
	 * @param partialSolution
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BackTrackSolution backtrack(BackTrackSolution current,
			BacktrackStopBag bag, DistanceMatrix distances,
			BackTrackSolution partialSolution) {
		
		if (Thread.interrupted())
			return partialSolution;

		Collection<? super TSPStop> all = bag.getAll();
		if (all.size() > 0) {
			List<TSPStop> candidates = null;
			candidates = new ArrayList<TSPStop>();
			candidates.addAll((Collection<? extends TSPStop>) all);

			// We try with all the candidates
			for (TSPStop stop : candidates) {
				bag.removeStop(stop);
				current.push(stop);

				// If we already have a solution candidate, is the current way
				// worse than it? If not, just use current solution as better
				if (!(partialSolution != null && current.getDistance(distances) > partialSolution
						.getDistance(distances))) {

					// Maybe another thread found a better solution
					if (partialSolution != null
							&& this.best.getSolution() != null) {
						if (partialSolution.getDistance(distances) < this.best
								.getSolution().getDistance(distances))
							partialSolution = (BackTrackSolution) this.best
									.getSolution().clone();

					}
					backtrack(current, bag, distances, partialSolution);
				}
				if (LOG.isTraceEnabled())
					LOG.trace("Current: " + current);

				current.pop();
				bag.addStop(stop);
			}
		} else { // We have reached the end of the way
			if (bag.hasLast()) {
				current.push(bag.getLast());

				if (current.getDistance(distances) < partialSolution
						.getDistance(distances)) {
					partialSolution.setStack(current.getStack());
				}

				current.pop();
			} else {
				if (current.getDistance(distances) < partialSolution
						.getDistance(distances)) {
					partialSolution.setStack(current.getStack());
				}
			}
		}

		partialSolution.getDistance(distances);
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