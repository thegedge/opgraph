@OpNodeInfo(
	name="String Splitter",
	description="Splits a string by whitespace or, if specified, a separator string.",
	category="String"
)
public class StringSplitter extends OpNode {
	public StringSplitter() {
		putField(new InputField("string", "input string", true, false, String.class));
		putField(new InputField("delimeter", "split delimeter", true, true, String.class));
		putField(new OutputField("split", "split string list", true, List.class))
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final String str = (String)context.get("string");
		
		String delim = "\\s";
		if(context.containsKey("delimeter")
			delim = (String)context.get("delimeter");
		
		context.put("split", Arrays.asList(str.split(delim)))
	}
}
