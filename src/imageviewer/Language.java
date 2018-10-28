package imageviewer;

public enum Language {
	ENGLISH("EN"), SWEDISH("SE"), FINNISH("FI");
	private String type;

	Language(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}
}