/*
 * Copyright 2015 POSEIDON Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.prop4j;

import java.util.List;

/**
 * A constraint that is true iff at least a specified number of children is
 * true.
 * 
 * @author Thomas Thum
 */
public class AtLeast extends Node {
	
	public int min;

	public AtLeast(int min, Object ...children) {
		this.min = min;
		setChildren(children);
	}

	public AtLeast(int min, Node[] children) {
		this.min = min;
		setChildren(children);
	}

	@Override
	protected Node eliminate(List<Class<? extends Node>> list) {
		super.eliminate(list);
		if (!list.contains(getClass()))
			return this;
		
		Node[] newNodes = chooseKofN(children, children.length - min + 1, false);
		return new And(newNodes);
	}

	@Override
	public Node clone() {
		return new AtLeast(min, clone(children));
	}

}
