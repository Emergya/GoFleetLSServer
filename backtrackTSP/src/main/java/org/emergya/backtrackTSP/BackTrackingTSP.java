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
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.openLS.tsp.TSPAlgorithm;
import org.gofleet.openLS.tsp.TSPStop;
import org.gofleet.openLS.tsp.TSPStopBag;

public class BackTrackingTSP implements TSPAlgorithm {
	private static Log LOG = LogFactory.getLog(BackTrackingTSP.class);

	public List<TSPStop> order(TSPStopBag _bag) {
		DistanceMatrix distances = new DistanceMatrix();

		if (!(_bag instanceof BacktrackStopBag)) {
			List<TSPStop> all = new LinkedList<TSPStop>();
			all.addAll(_bag.getAll());
			_bag = new BacktrackStopBag(all, _bag.getFirst(), _bag.getLast());
		}

		BacktrackStopBag bag = (BacktrackStopBag) _bag;
		BackTrackSolution res = new BackTrackSolution(new Stack<TSPStop>());

		if (bag.hasFirst()) {
			res.push(bag.getFirst());
		}

		BackTrackSolution best = backtrack(res, bag, distances,
				new BackTrackSolution(new Stack<TSPStop>()));

		if (best != null && best.getStack().size() == bag.size())
			return best.getStack();

		throw new RuntimeException(
				"I'm embarrased, I was unable to find a solution.");
	}

	private BackTrackSolution backtrack(BackTrackSolution current,
			BacktrackStopBag bag, DistanceMatrix distances,
			BackTrackSolution best) {

		Collection<TSPStop> all = bag.getAll();

		final int size = all.size();
		if (size > 0) {
			List<TSPStop> candidates = new ArrayList<TSPStop>(size);
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

}

class BackTrackSolution {
	private Stack<TSPStop> res;
	private Double cachedDistance;

	protected BackTrackSolution(Stack<TSPStop> res) {
		this.cachedDistance = -1d;
		this.res = res;
	}

	protected Stack<TSPStop> getStack() {
		return this.res;
	}

	protected void setStack(Stack<TSPStop> stack) {
		this.cachedDistance = -1d;
		this.res.clear();
		this.res.addAll(stack);
	}

	protected TSPStop pop() {
		this.cachedDistance = -1d;
		return this.res.pop();
	}

	protected TSPStop push(TSPStop stop) {
		this.cachedDistance = -1d;
		return this.res.push(stop);
	}

	public Double getDistance(DistanceMatrix distance) {
		if (this.res.size() == 0) {
			this.cachedDistance = Double.MAX_VALUE;
			return Double.MAX_VALUE;
		}

		if (this.cachedDistance > 0)
			return this.cachedDistance;

		Double cost = 0d;
		TSPStop last = null;
		for (TSPStop stop : this.res) {
			if (last != null) {
				cost += distance.distance((BacktrackStop) last,
						(BacktrackStop) stop);
			}
			last = stop;
		}

		this.cachedDistance = cost;
		return cost;
	}

	@Override
	public String toString() {
		String s = "[" + this.cachedDistance + ": ";
		for (TSPStop stop : this.res)
			s += stop.toString() + " ";
		s += "]";
		return s;
	}
}
