package de.vonfelix.tileserver.tile;

import java.util.ArrayList;
import java.util.Arrays;

import de.vonfelix.tileserver.Color;

/*
 * An array for each Parameter
 * 
 */

public class Parameters {
	private Color[] colors;
	private Integer[] min_values;
	private Integer[] max_values;
	private Double[] exponents;

	public Parameters( Color[] colors, Integer[] min_values, Integer[] max_values, Double[] exponents ) {
		this.colors = colors == null ? new Color[] {} : colors;
		this.min_values = min_values == null ? new Integer[] {} : min_values;
		this.max_values = max_values == null ? new Integer[] {} : max_values;
		this.exponents = exponents == null ? new Double[] {} : exponents;
	}

	public Parameters( String adjCol, String adjMin, String adjMax, String adjExp ) {
		ArrayList<Color> colors = new ArrayList<>();
		ArrayList<Integer> min_values = new ArrayList<>();
		ArrayList<Integer> max_values = new ArrayList<>();
		ArrayList<Double> exponents = new ArrayList<>();

		// insert LAMBDAs here... (?)

		if ( adjCol != null ) {
			for ( String e : adjCol.split( "\\s*,\\s*" ) ) {
				switch ( e.toLowerCase() ) {
				case "red":
				case "r":
					colors.add( Color.RED );
					break;
				case "green":
				case "g":
					colors.add( Color.GREEN );
					break;
				case "blue":
				case "b":
					colors.add( Color.BLUE );
					break;
				case "cyan":
				case "c":
					colors.add( Color.CYAN );
					break;
				case "magenta":
				case "m":
					colors.add( Color.MAGENTA );
					break;
				case "yellow":
				case "y":
					colors.add( Color.YELLOW );
					break;
				case "grays":
				case "gray":
				case "greys":
				case "grey":
				case "whites":
				case "white":
				case "w":
					colors.add( Color.GRAYS );
					break;
				}
			}
		}
		if ( adjMin != null ) {
			for ( String e : adjMin.split( "\\s*,\\s*" ) ) {
				min_values.add( Integer.parseInt( e ) );
			}
		}
		if ( adjMax != null ) {
			for ( String e : adjMax.split( "\\s*,\\s*" ) ) {
				max_values.add( Integer.parseInt( e ) );
			}
		}
		if ( adjExp != null ) {
			for ( String e : adjExp.split( "\\s*,\\s*" ) ) {
				exponents.add( Double.parseDouble( e ) );
			}
		}

		this.colors = colors.toArray( new Color[ colors.size() ] );
		this.min_values = min_values.toArray( new Integer[ min_values.size() ] );
		this.max_values = max_values.toArray( new Integer[ max_values.size() ] );
		this.exponents = exponents.toArray( new Double[ exponents.size() ] );
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

	@Override
	public String toString() {
		return Arrays.toString( colors ) + Arrays.toString( min_values ) + Arrays.toString( max_values ) + Arrays.toString( exponents );
		//return "[" + Arrays.stream( colors ).forEach(Color::name ) + "|" + min_values + "|" + max_values + "|" + exponents + "]";
	}
}
