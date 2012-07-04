/*
 *  Copyright (C) 2011-2012 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
 *
 *  Animotron is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Animotron is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of
 *  the GNU Affero General Public License along with Animotron.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.animotron.animi;

import static org.neo4j.graphdb.Direction.OUTGOING;

import org.animotron.graph.AnimoGraph;
import org.animotron.manipulator.OnQuestion;
import org.animotron.manipulator.PFlow;
import org.animotron.statement.operator.AN;
import org.animotron.statement.operator.Operator;
import org.animotron.statement.operator.Prepare;
import org.animotron.statement.operator.REF;
import org.animotron.statement.value.VALUE;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Label extends Operator implements Prepare {
	
	public static final Label _ = new Label();
	
    private static final String name = "label";

    private Label() { super(name); }
	
	@Override
	public OnQuestion onPrepareQuestion() {
		return new Prepare();
	}
	
	public static TraversalDescription td =
			Traversal.description().
				depthFirst().
				uniqueness(Uniqueness.RELATIONSHIP_GLOBAL).
				relationships(AN._, OUTGOING).
				relationships(VALUE._, OUTGOING);

	class Prepare extends OnQuestion {
		public boolean needAnswer() {
			return false;
		}

		@Override
    	public void act(final PFlow pf) throws Throwable {
			Transaction tx = AnimoGraph.beginTx();
			try {
				String label = null;
				for (Path path : td.traverse(pf.getOPNode())) {
					if (path.length() == 0) continue;
					
					Relationship r = path.lastRelationship();
					if (r.isType(VALUE._)) {
						if (r.getEndNode().hasRelationship(AN._, OUTGOING))
							continue;
							
						label = VALUE._.reference(r).toString();
						
						//add to index
						Labels.add(r, label);
						
						//build letters structure
						for (int i = label.length() - 1; i >= 0; i--) {
							//create AN
							r = r.getEndNode().createRelationshipTo(AnimoGraph.createNode(), AN._);
							//create REF
							r.getEndNode().createRelationshipTo(Letters.getDef(label.charAt(i)), REF._);
						}
					}
				}
				tx.success();
			} finally {
				AnimoGraph.finishTx(tx);
			}
			System.out.println("done");
		}
	}
}