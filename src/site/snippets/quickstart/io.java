// Get the default serializer
final OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
if(serializer == null)
	; // Be sure to handle the case when no serialization mechanism is available

// Write a graph to a stream
try {
	serializer.write(graph, System.out);
} catch(IOException exc) {
	// Handle the exception
}

// Read a graph from a stream
OpGraph graph = null;
try {
	graph = serializer.read(System.in);
} catch(IOException exc) {
	// Handle the exception
}
