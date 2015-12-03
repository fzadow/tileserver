package de.vonfelix;

import de.vonfelix.Channel.Color;

/*
 * An array for each Parameter
 * 
 */

public class TileParameters {
	private Color[] colors;
	private Integer[] min_values;
	private Integer[] max_values;
	private Float[] exponents;

	public TileParameters( Color[] colors, Integer[] min_values, Integer[] max_values, Float[] exponents ) {
		this.colors = colors;
		this.min_values = min_values;
		this.max_values = max_values;
		this.exponents = exponents;
	}

	public Color[] getColors() {
		return colors;
	}

	public Integer[] getMinValues() {
		return min_values;
	}

	public Integer[] getMaxValues() {
		return max_values;
	}

	public Float[] getExponents() {
		return exponents;
	}
}
