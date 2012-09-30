final OpContext context = new OpContext();
final Processor processor = new Processor(graph);
processor.reset(context);
processor.stepAll();
if(processor.getError() != null) {
	// Handle this error
} else {
	// If necessary, grab results from context
}
