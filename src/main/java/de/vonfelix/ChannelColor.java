package de.vonfelix;

public class ChannelColor {
	public static final int RED =     0b111111110000000000000000;
	public static final int GREEN =   0b000000001111111100000000;
	public static final int BLUE =    0b000000000000000011111111;
	public static final int CYAN =    0b000000001111111111111111;
	public static final int MAGENTA = 0b111111110000000011111111;
	public static final int YELLOW =  0b111111111111111100000000;
	public static final int GRAYS =   0b111111111111111111111111;
	
	public static int getColorValue( ColorName colorName ) {
		switch ( colorName ) {
		case RED : return RED;
		case GREEN : return GREEN;
		case BLUE : return BLUE;
		case CYAN : return CYAN;
		case MAGENTA : return MAGENTA;
		case YELLOW : return YELLOW;
		case GRAYS : return GRAYS;
		}
		return 0;
	}

	public enum ColorName  {
		RED,
		GREEN,
		BLUE,
		CYAN,
		MAGENTA,
		YELLOW,
		GRAYS
	}

}
