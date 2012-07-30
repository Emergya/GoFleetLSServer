package org.emergya.backtrackTSP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.openLS.tsp.TSPStop;
import org.gofleet.openLS.tsp.TSPStopBag;

class AStar implements Runnable {
	private static Log LOG = LogFactory.getLog(AStar.class);

	private DistanceMatrix distances;
	private TSPStopBag bag;
	private SolutionContainer best;
	private TreeMap<Double, Stack<TSPStop>> openCandidates = new TreeMap<Double, Stack<TSPStop>>();
	private List<TSPStop> stops = new ArrayList<TSPStop>();

	public AStar(TSPStopBag _bag, DistanceMatrix distances,
			SolutionContainer best) {
		this.distances = distances;
		this.bag = getBacktrackingBag(_bag);
		this.best = best;
		this.stops.addAll(_bag.getAll());
	}

	public void run() {
		try {
			Stack<TSPStop> stack = new Stack<TSPStop>();
			stack.add(this.bag.getFirst());
			openCandidates.put(-1d, stack);
			BackTrackSolution solution = astar(this.distances,
					new BackTrackSolution(new Stack<TSPStop>()));
			best.add(solution);
		} catch (Throwable t) {
			LOG.error("Failure on astar procedure.", t);
		}
	}

	private BackTrackSolution astar(DistanceMatrix distances,
			BackTrackSolution partialSolution) throws InterruptedException {

		Entry<Double, Stack<TSPStop>> candidate = openCandidates.firstEntry();

		if (candidate == null)
			return partialSolution;

		openCandidates.remove(candidate.getKey());
		BackTrackSolution current = new BackTrackSolution(candidate.getValue());

		while (this.bag.size() != current.getStack().size()) {
			Double distance = current.getDistance(distances);

			List<TSPStop> candidates = new LinkedList<TSPStop>();
			candidates.addAll(this.stops);
			candidates.removeAll(current.getStack());

			for (TSPStop stop : candidates) {
				Stack<TSPStop> newstack = new Stack<TSPStop>();
				newstack.addAll(current.getStack());

				TSPStop end = this.bag.getLast();
				TSPStop from = newstack.peek();

				newstack.add(stop);
				openCandidates.put(
						(distance
								+ distances.distance((BacktrackStop) from,
										(BacktrackStop) stop) + stop
								.getPosition().distance(end.getPosition())),
						newstack);

			}
			candidate = openCandidates.firstEntry();

			if (candidate == null)
				return partialSolution;

			openCandidates.remove(candidate.getKey());
			current = new BackTrackSolution(candidate.getValue());

		}

		return current;

	}

	private BacktrackStopBag getBacktrackingBag(TSPStopBag _bag) {
		List<TSPStop> all = new LinkedList<TSPStop>();
		all.addAll((Collection<? extends TSPStop>) _bag.getAll());
		return new BacktrackStopBag(all, _bag.getFirst(), _bag.getLast());
	}

}