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
	private List<BackTrackSolution> best;

	public Backtracking(TSPStopBag _bag, DistanceMatrix distances,
			List<BackTrackSolution> best) {
		this.current = initializeResBag(_bag);
		this.bag = getBacktrackingBag(_bag);
		this.distances = distances;
		this.best = best;
	}

	@Override
	public void run() {
		BackTrackSolution solution = backtrack(this.current, this.bag,
				this.distances, new BackTrackSolution(new Stack<TSPStop>()));
		best.add(solution);
	}

	private BackTrackSolution backtrack(BackTrackSolution current,
			BacktrackStopBag bag, DistanceMatrix distances,
			BackTrackSolution best) {

		Collection<TSPStop> all = bag.getAll();
		if (all.size() > 0) {
			List<TSPStop> candidates = null;
			candidates = new ArrayList<TSPStop>();
			candidates.addAll(all);
			for (TSPStop stop : candidates) {
				bag.removeStop(stop);
				current.push(stop);

				if (!(best != null && current.getDistance(distances) > best
						.getDistance(distances))) {
					backtrack(current, bag, distances, best);
				}
				if (LOG.isTraceEnabled())
					LOG.trace("Current: " + current);

				current.pop();
				bag.addStop(stop);
			}
		} else {
			if (bag.hasLast()) {
				current.push(bag.getLast());

				if (current.getDistance(distances) < best
						.getDistance(distances)) {
					best.setStack(current.getStack());
				}

				current.pop();
			} else {
				if (current.getDistance(distances) < best
						.getDistance(distances)) {
					best.setStack(current.getStack());
				}
			}
		}

		best.getDistance(distances);
		return best;
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
		all.addAll(_bag.getAll());
		return new BacktrackStopBag(all, _bag.getFirst(), _bag.getLast());
	}

}