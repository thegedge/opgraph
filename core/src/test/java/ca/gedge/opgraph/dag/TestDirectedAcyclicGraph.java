/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * This file is part of the OpGraph project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.gedge.opgraph.dag;

import static org.junit.Assert.*;
import static ca.gedge.CollectionsAssert.assertCollectionEqualsArray;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.DirectedAcyclicGraph;
import ca.gedge.opgraph.dag.SimpleDirectedEdge;
import ca.gedge.opgraph.dag.Vertex;
import ca.gedge.opgraph.dag.VertexNotFoundException;

/**
 * Tests {@link DirectedAcyclicGraph}.
 */
public class TestDirectedAcyclicGraph {
	/**
	 * Basic vertex class for testing.
	 */
	private static class SimpleVertex implements Vertex {
		private String name;
		
		public SimpleVertex(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	//
	// Test data
	//
	
	private HashMap<String, SimpleVertex> vertexMap = new HashMap<String, SimpleVertex>();
	private HashMap<String, SimpleDirectedEdge<SimpleVertex>> edgeMap = new HashMap<String, SimpleDirectedEdge<SimpleVertex>>();
	
	@Before
	public void setUp() {
		char start = 'A';
		char end = 'Z';
		for(char s = start; s <= end; ++s)
			vertexMap.put("" + s, new SimpleVertex("" + s));
		
		for(char u = start; u <= end; ++u) {
			for(char v = (char)(u + 1); v <= end; ++v) {
				SimpleVertex uV = vertexMap.get("" + u);
				SimpleVertex vV = vertexMap.get("" + v);
				edgeMap.put(u + "" + v, new SimpleDirectedEdge<SimpleVertex>(uV, vV));
				edgeMap.put(v + "" + u, new SimpleDirectedEdge<SimpleVertex>(vV, uV));
			}
		}
	}
	
	/**
	 * Tests the correctness of topological sorting in a DAG
	 */
	@Test
	public void testTopologicalOrdering() {
		DirectedAcyclicGraph<SimpleVertex, SimpleDirectedEdge<SimpleVertex>> dag = new DirectedAcyclicGraph<SimpleVertex, SimpleDirectedEdge<SimpleVertex>>();
		dag.add(vertexMap.get("A"));
		dag.add(vertexMap.get("B"));
		dag.add(vertexMap.get("C"));
		dag.add(vertexMap.get("D"));
		
		try {
			dag.add(edgeMap.get("DC"));
			dag.add(edgeMap.get("CA"));
			dag.add(edgeMap.get("AB"));
			dag.add(edgeMap.get("DA"));
		} catch(VertexNotFoundException exc) {
			fail("Vertex not found, but should be: " + exc.getVertex());
		} catch(CycleDetectedException exc) {
			fail("Adding edge creates cycle, but this shouldn't happen");
		}
		
		assertCollectionEqualsArray(dag.getVertices(), 
		                            vertexMap.get("A"), vertexMap.get("B"), vertexMap.get("C"), vertexMap.get("D"));
	}
	
	/**
	 * Tests cycle detection in a DAG
	 */
	@Test(expected=CycleDetectedException.class)
	public void testCycleException() throws CycleDetectedException {
		DirectedAcyclicGraph<SimpleVertex, SimpleDirectedEdge<SimpleVertex>> dag = new DirectedAcyclicGraph<SimpleVertex, SimpleDirectedEdge<SimpleVertex>>();
		dag.add(vertexMap.get("A"));
		dag.add(vertexMap.get("B"));
		dag.add(vertexMap.get("C"));
		dag.add(vertexMap.get("D"));
		
		try {
			dag.add(edgeMap.get("DC"));
			dag.add(edgeMap.get("CA"));
			dag.add(edgeMap.get("AB"));
			dag.add(edgeMap.get("DA"));
			dag.add(edgeMap.get("BD")); // creates a cycle
		} catch(VertexNotFoundException exc) {
			fail("Vertex not found, but should be: " + exc.getVertex());
		}
	}
	
	/**
	 * Tests incoming/outgoing edges in a DAG
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testIncomingOutgoingEdges() {
		DirectedAcyclicGraph<SimpleVertex, SimpleDirectedEdge<SimpleVertex>> dag = new DirectedAcyclicGraph<SimpleVertex, SimpleDirectedEdge<SimpleVertex>>();
		dag.add(vertexMap.get("A"));
		dag.add(vertexMap.get("B"));
		dag.add(vertexMap.get("C"));
		dag.add(vertexMap.get("D"));
		dag.add(vertexMap.get("E"));
		dag.add(vertexMap.get("F"));
		dag.add(vertexMap.get("G"));
		
		try {
			dag.add(edgeMap.get("DC"));
			dag.add(edgeMap.get("CA"));
			dag.add(edgeMap.get("AB"));
			dag.add(edgeMap.get("AE"));
			dag.add(edgeMap.get("DA"));
			dag.add(edgeMap.get("EF"));
		} catch(CycleDetectedException exc) {
			fail("Adding edge creates cycle, but this shouldn't happen");
		} catch(VertexNotFoundException exc) {
			fail("Vertex not found, but should be: " + exc.getVertex());
		}
		
		assertCollectionEqualsArray(dag.getIncomingEdges(vertexMap.get("A")), edgeMap.get("CA"), edgeMap.get("DA"));
		assertCollectionEqualsArray(dag.getOutgoingEdges(vertexMap.get("A")), edgeMap.get("AB"), edgeMap.get("AE"));
		
		assertCollectionEqualsArray(dag.getIncomingEdges(vertexMap.get("B")), edgeMap.get("AB"));
		assertCollectionEqualsArray(dag.getOutgoingEdges(vertexMap.get("B")));
		
		assertCollectionEqualsArray(dag.getIncomingEdges(vertexMap.get("C")), edgeMap.get("DC"));
		assertCollectionEqualsArray(dag.getOutgoingEdges(vertexMap.get("C")), edgeMap.get("CA"));
		
		assertCollectionEqualsArray(dag.getIncomingEdges(vertexMap.get("D")));
		assertCollectionEqualsArray(dag.getOutgoingEdges(vertexMap.get("D")), edgeMap.get("DA"), edgeMap.get("DC"));
		
		assertCollectionEqualsArray(dag.getIncomingEdges(vertexMap.get("E")), edgeMap.get("AE"));
		assertCollectionEqualsArray(dag.getOutgoingEdges(vertexMap.get("E")), edgeMap.get("EF"));
		
		assertCollectionEqualsArray(dag.getIncomingEdges(vertexMap.get("F")), edgeMap.get("EF"));
		assertCollectionEqualsArray(dag.getOutgoingEdges(vertexMap.get("F")));
		
		assertCollectionEqualsArray(dag.getIncomingEdges(vertexMap.get("G")));
		assertCollectionEqualsArray(dag.getOutgoingEdges(vertexMap.get("G")));
	}
}
