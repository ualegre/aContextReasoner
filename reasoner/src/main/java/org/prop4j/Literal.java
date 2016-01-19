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
 * A variable or negated variable.
 * 
 * @author Thomas Thum
 */
public class Literal extends Node {
	
	public Object var;
	
	public boolean positive;

	public Literal(Object var, boolean positive) {
		this.var = var;
		this.positive = positive;
	}
	
	public Literal(Object var) {
		this.var = var;
		positive = true;
	}

	public void flip() {
		positive = !positive;
	}
	
	@Override
	protected Node eliminate(List<Class<? extends Node>> list) {
		//nothing to do with children
		return this;
	}
	
	@Override
	protected Node clausify() {
		//nothing to do
		return this;
	}
	
	@Override
	public void simplify() { 
		//nothing to do (recursive calls reached lowest node)
	}

	@Override
	public Node clone() {
		return new Literal(var, positive);
	}

	@Override
	public boolean equals(Object node) {
		if (!(node instanceof Literal))
			return false;
		return (var.equals(((Literal) node).var)) && (positive == ((Literal) node).positive);
	}
	
}
