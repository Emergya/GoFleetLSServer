package org.emergya.backtrackTSP;

import java.util.Stack;

import org.gofleet.openLS.tsp.TSPStop;

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