package de.vonfelix.tileserver;

/*
 * An array for each Parameter
 * 
 */

public class TileParameters {
	private Color[] colors;
	private Integer[] min_values;
	private Integer[] max_values;
	private Double[] exponents;

	public TileParameters( Color[] colors, Integer[] min_values, Integer[] max_values, Double[] exponents ) {
		this.colors = colors == null ? new Color[] {} : colors;
		this.min_values = min_values == null ? new Integer[] {} : min_values;
		this.max_values = max_values == null ? new Integer[] {} : max_values;
		this.exponents = exponents == null ? new Double[] {} : exponents;
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

	public Double[] getExponents() {
		return exponents;
	}
}
