@Override
public void operate(OpContext context) throws ProcessingException {
	final String str = (String)context.get("string");
	
	String delim = "\\s";
	if(context.containsKey("delimeter")
		delim = (String)context.get("delimeter");
	
	context.put("split", Arrays.asList(str.split(delim)))
}
