final OpGraph graph = new OpGraph();

final RandomInteger intA = new RandomInteger();
final RandomInteger intB = new RandomInteger();
final Adder adder = new Adder();

graph.add(intA);
graph.add(intB);
graph.add(adder);

try {
	graph.add(new OpLink(intA, "value", adder, "x"));	
	graph.add(new OpLink(intB, "value", adder, "y"));	
} catch(CycleDetectedException exc) {
	// If adding a link creates a cycle
} catch(ItemMissingException exc) {
	// If a field specified in the edge constructor does not exist
	// in its corresponding node
}
