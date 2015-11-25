package de.vonfelix;

import java.util.HashMap;

public class Channel {

	public enum Color {
		RED, GREEN, BLUE, CYAN, MAGENTA, YELLOW, GRAYS
	}

	private static final HashMap<Color,Integer> colors = new HashMap<Color,Integer>() {
		{
			put( Color.RED, 0b111111110000000000000000 );
			put( Color.GREEN, 0b111111110000000000000000 );
			put( Color.BLUE, 0b111111110000000000000000 );
			put( Color.CYAN, 0b111111110000000000000000 );
			put( Color.MAGENTA, 0b111111110000000000000000 );
			put( Color.YELLOW, 0b111111110000000000000000 );
			put( Color.GRAYS, 0b111111110000000000000000 );
		}
	};

	private Stack stack;
	private Color color;

	public Channel( Stack stack, Color color ) {
		this.stack= stack;
		this.color = color;
	}

	public Stack getStack() {
		return stack;
	}

	public Color getColor() {
		return color;
	}

	public int getColorValue() {
		return colors.get( color );
	}

}
