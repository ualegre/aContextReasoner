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
 * A constraint that is true iff the left child is false or the right child is
 * true.
 * 
 * @author Thomas Thum
 */
public class Implies extends Node {
	
	public Implies(Object leftChild, Object rightChild) {
		setChildren(leftChild, rightChild);
	}
	
	@Override
	protected Node eliminate(List<Class<? extends Node>> list) {
		super.eliminate(list);
		if (list.contains(getClass()))
			return new Or(new Not(children[0]), children[1]);
		return this;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!getClass().isInstance(object))
			return false;
		Implies implies = (Implies) object;
		return children[0].equals(implies.children[0]) && children[1].equals(implies.children[1]);
	}
	
	@Override
	public Node clone() {
		return new Implies(children[0].clone(), children[1].clone());
	}

}
