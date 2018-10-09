package marketing.satori.kintone.csv.exelog;

public enum Operation {
	IMPORT("import"),
	EXPORT("export");

	private String value;

	private Operation(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}
}
