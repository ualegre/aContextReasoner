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
 * A constraint that is true iff exactly a specified number of children is
 * true.
 * 
 * @author Thomas Thum
 */
public class Choose extends Node {
	
	public int n;

	public Choose(int n, Object ...children) {
		this.n = n;
		setChildren(children);
	}

	public Choose(int n, Node[] children) {
		this.n = n;
		setChildren(children);
	}

	@Override
	protected Node eliminate(List<Class<? extends Node>> list) {
		super.eliminate(list);
		if (!list.contains(getClass()))
			return this;

		return new And(new AtMost(n, clone(children)), new AtLeast(n, clone(children)));
	}

	@Override
	public Node clone() {
		return new Choose(n, clone(children));
	}

}
