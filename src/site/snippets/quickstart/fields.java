public StringSplitter() {
	putField(new InputField("string", "input string", true, false, String.class));
	putField(new InputField("delimeter", "split delimeter", true, true, String.class));
	putField(new OutputField("split", "split string list", true, List.class))
}
