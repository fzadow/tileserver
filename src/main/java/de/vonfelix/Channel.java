package de.vonfelix;

import de.vonfelix.ChannelColor.ColorName;

public class Channel {
	private Stack stack;
	private ColorName colorName;

	public Channel( Stack stack, ColorName colorName ) {
		this.stack= stack;
		this.colorName = colorName;
		
	}

	public Stack getStack() {
		return stack;
	}

	public ColorName getColorName() {
		return colorName;
	}

	public int getColor() {
		return ChannelColor.getColor( colorName );
	}

	public int getColorValue() {
		return colors.get( color );
	}

}
