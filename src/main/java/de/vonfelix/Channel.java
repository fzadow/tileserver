package de.vonfelix;

import java.util.HashMap;

public class Channel implements ValueLimit {

	public enum Color {
		RED, GREEN, BLUE, CYAN, MAGENTA, YELLOW, GRAYS
	}

	private static final HashMap<Color,Integer> colors = new HashMap<Color,Integer>() {
		{
			put( Color.RED, 0b111111110000000000000000 );
			put( Color.GREEN, 0b000000001111111100000000 );
			put( Color.BLUE, 0b000000000000000011111111 );
			put( Color.CYAN, 0b000000001111111111111111 );
			put( Color.MAGENTA, 0b111111110000000011111111 );
			put( Color.YELLOW, 0b111111111111111100000000 );
			put( Color.GRAYS, 0b111111111111111111111111 );
		}
	};

	private SimpleStack simpleStack;
	private Color color;
	private int valueLimit;

	public Channel( SimpleStack simpleStack, Color color ) {
		this.simpleStack= simpleStack;
		this.color = color;
	}

	public SimpleStack getStack() {
		return simpleStack;
	}

	public Color getColor() {
		return color;
	}

	public int getColorValue() {
		return colors.get( color );
	}

	@Override
	public void setValueLimit( int valueLimit ) {
		this.valueLimit = valueLimit;
	}

	@Override
	public int getValueLimit() {
		if ( valueLimit == 0 ) {
			return simpleStack.getValueLimit();
		}
		return valueLimit;
	}

	@Override
	public String toString() {
		return simpleStack + "(" + color + ")";
	}
}