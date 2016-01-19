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
 * A constraint that is true iff the child node is false.
 * 
 * @author Thomas Thum
 */
public class Not extends Node {
	
	public Not(Object child) {
		children = new Node[] { getNode(child) };
	}
	
	@Override
	protected Node eliminate(List<Class<? extends Node>> list) {
		Node node = children[0];
		if (!list.contains(getClass())) {
			children[0] = node.eliminate(list);
			return this;
		}
		
		//reduce Not(Literal) to Literal
		if (node instanceof Literal) {
			((Literal) node).flip();
			return node;
		}
		//reduce Not(Not(Node)) to Node
		if (node instanceof Not) {
			return ((Not) node).children[0].eliminate(list);
		}
		//transform Not(And(a,b)) to Or(Not(a),Not(b))
		if (node instanceof And) {
			negateNodes(node.children);
			node.eliminate(list);
			return new Or((Object[]) node.children);
		}
		//transform Not(Or(a,b)) to And(Not(a),Not(b))
		if (node instanceof Or) {
			negateNodes(node.children);
			node.eliminate(list);
			return new And((Object[]) node.children);
		}
		//transform Not(AtMostx(a,b)) to AtLeastx+1(a,b)
		if (node instanceof AtMost) {
			node.eliminate(list);
			return new AtLeast(((AtMost) node).max + 1, (Object[]) node.children);
		}
		//transform Not(AtLeastx(a,b)) to AtMostx-1(a,b)
		if (node instanceof AtLeast) {
			node.eliminate(list);
			return new AtMost(((AtLeast) node).min - 1, (Object[]) node.children);
		}
		throw new RuntimeException(node.getClass().getName() + " is not supported");
	}

	@Override
	public Node clone() {
		return new Not(children[0].clone());
	}

}
