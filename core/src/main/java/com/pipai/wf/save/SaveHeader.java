package com.pipai.wf.save;

public enum SaveHeader {
	VARIABLES("Variables"), PARTY("Party");

	private String headerText;

	SaveHeader(String headerText) {
		this.headerText = headerText;
	}

	@Override
	public String toString() {
		return "[" + headerText + "]";
	}

	public static SaveHeader getHeader(String line) {
		for (SaveHeader header : SaveHeader.values()) {
			if (header.toString().equals(line)) {
				return header;
			}
		}
		return null;
	}
}
