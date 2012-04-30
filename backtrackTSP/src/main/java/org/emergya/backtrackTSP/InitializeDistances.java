package org.emergya.backtrackTSP;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InitializeDistances extends Thread {
	private static Log LOG = LogFactory.getLog(InitializeDistances.class);

	private BacktrackStop from;
	private List<BacktrackStop> to;
	private DistanceMatrix distance;

	public InitializeDistances(BacktrackStop from, List<BacktrackStop> to,
			DistanceMatrix distance) {
		super();
		this.from = from;
		this.to = to;
		this.distance = distance;
	}

	public void run() {
		for (BacktrackStop i : to) {
			if (i != from)
				distance.distance(from, i);
		}
		LOG.trace("Done with " + from.getId());
	}

}