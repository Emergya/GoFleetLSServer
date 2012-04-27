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
import java.util.List;

import org.gofleet.openLS.tsp.TSPStop;
import org.gofleet.openLS.tsp.TSPStopBag;

public class BacktrackStopBag implements TSPStopBag {

	private TSPStop first = null;
	private TSPStop last = null;
	private List<TSPStop> bag = new ArrayList<TSPStop>();

	public TSPStop getFirst() {
		return this.first;
	}

	public TSPStop getLast() {
		return this.last;
	}

	public Collection<TSPStop> getAll() {
		return this.bag;
	}

	public Boolean hasFirst() {
		return getFirst() != null;
	}

	public Boolean hasLast() {
		return getLast() != null;
	}

	public BacktrackStopBag(List<TSPStop> stops) {
		super();
		this.bag.addAll(stops);
	}

	public BacktrackStopBag(List<TSPStop> stops, TSPStop first, TSPStop last) {
		this(stops);
		this.first = first;
		this.last = last;
	}

	public void removeStop(TSPStop stop){
		this.bag.remove(stop);
	}

	public void addStop(TSPStop stop){
		this.bag.add(stop);
	}

	public int size() {
		int size = 0;
		if(this.hasFirst())
			size++;
		if(this.hasLast())
			size++;
		return size + this.bag.size();
	}
	
	@Override
	public String toString() {
		String s = "{";
		
		for(TSPStop stop : this.bag)
			s += stop.getPosition().toText() + " ";
		
		s += "}";
		
		return s;
	}
}
