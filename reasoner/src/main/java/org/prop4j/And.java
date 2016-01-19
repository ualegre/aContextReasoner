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

import java.util.ArrayList;
import java.util.List;

/**
 * A constraint that is true iff all child nodes are true.
 * 
 * @author Thomas Thum
 */
public class And extends Node {

	public And(Object ...children) {
		setChildren(children);
	}
	
	public And(Node[] children) {
		setChildren(children);
	}

	@Override
	protected Node clausify() {
		for (int i = 0; i < children.length; i++)
			children[i] = children[i].clausify();
		fuseWithSimilarChildren();
		return this;
	}
	
	protected void collectChildren(Node node, List<Node> nodes) {
		if (node instanceof And) {
			for (Node childNode : node.getChildren()) {
				collectChildren(childNode, nodes);
			}
		} else {
			nodes.add(node);
		}
	}
	
	@Override
	public void simplify() {
		List<Node> nodes = new ArrayList<Node>();
		
		for (int i = 0; i < children.length; i++) {
			collectChildren(children[i], nodes);
		}
		
		if (nodes.size() != children.length) {
			Node[] newChildren = nodes.toArray(new Node[nodes.size()]);
			setChildren(newChildren);
		}
		
		super.simplify();
	}
	
	@Override
	public Node clone() {
		return new And(clone(children));
	}

}
