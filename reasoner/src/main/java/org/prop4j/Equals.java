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
 * A constraint that is true iff both children have the same boolean value.
 * 
 * @author Thomas Thum
 */
public class Equals extends Node {
	
	public Equals(Object leftChild, Object rightChild) {
		setChildren(leftChild, rightChild);
	}
	
	@Override
	protected Node eliminate(List<Class<? extends Node>> list) {
		super.eliminate(list);
		if (list.contains(getClass()))
			return new And(new Or(new Not(children[0].clone()), children[1]),
					new Or(new Not(children[1].clone()), children[0]));
		return this;
	}
	
	@Override
	public Node clone() {
		return new Equals(children[0].clone(), children[1].clone());
	}
	
}
