package de.vonfelix.tileserver;

public enum Color {
	RED(0b111111110000000000000000, 0b11111111, 0b00000000, 0b00000000),
	GREEN(0b000000001111111100000000, 0b00000000, 0b11111111, 0b00000000),
	BLUE(0b000000000000000011111111, 0b00000000, 0b00000000, 0b11111111),
	CYAN(0b000000001111111111111111, 0b00000000, 0b11111111, 0b11111111),
	MAGENTA(0b111111110000000011111111, 0b11111111, 0b00000000, 0b11111111),
	YELLOW(0b111111111111111100000000, 0b11111111, 0b11111111, 0b00000000),
	GRAYS(0b111111111111111111111111, 0b11111111, 0b11111111, 0b11111111);

	private final int value;
	private final int r;
	private final int g;
	private final int b;

	Color( int value, int r, int g, int b ) {
		this.value = value;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	int value() {
		return value;
	}

	int r() {
		return r;
	}

	int g() {
		return g;
	}

	int b() {
		return b;
	}

}
