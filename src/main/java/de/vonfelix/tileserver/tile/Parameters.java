package de.vonfelix.tileserver.tile;

import java.util.ArrayList;
import java.util.HashMap;

import de.vonfelix.tileserver.Color;

/*
 * An array for each Parameter
 * 
 */

public class Parameters {
	private HashMap<Parameter, Object> parameters = new HashMap<>();

	@SuppressWarnings( "rawtypes" )
	public enum Parameter {
		COLORS(Color[].class),
		MINVALUES(Integer[].class),
		MAXVALUES(Integer[].class),
		EXPONENTS(Double[].class),
		QUALITY(Float.class),
		WIDTH(Integer.class),
		HEIGHT(Integer.class);

		private final Class clazz;

		Parameter( Class clazz ) {
			this.clazz = clazz;
		}

		public Class getClazz() {
			return clazz;
		}
	}

	private Parameters() {
	}

	public static class Builder {
		private Color[] colors;
		private Integer[] min_values;
		private Integer[] max_values;
		private Double[] exponents;
		private Float quality;
		private int width;
		private int height;

		public Builder() {
		}

		public Builder colors( Color[] colors ) {
			this.colors = colors;
			return this;
		}

		public Builder colors( String adjCol ) {
			if ( adjCol != null ) {
				ArrayList<Color> colors = new ArrayList<>();
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
				this.colors = colors.toArray( new Color[ colors.size() ] );
			}
			return this;
		}

		public Builder min_values( Integer[] min_values ) {
			this.min_values = min_values;
			return this;
		}

		public Builder min_values( String adjMin ) {
			if ( adjMin != null ) {
				ArrayList<Integer> min_values = new ArrayList<>();
				for ( String e : adjMin.split( "\\s*,\\s*" ) ) {
					min_values.add( Integer.parseInt( e ) );
				}
				this.min_values = min_values.toArray( new Integer[ min_values.size() ] );
			}
			return this;
		}

		public Builder max_values( Integer[] max_values ) {
			this.max_values = max_values;
			return this;
		}

		public Builder max_values( String adjMax ) {
			if ( adjMax != null ) {
				ArrayList<Integer> max_values = new ArrayList<>();
				for ( String e : adjMax.split( "\\s*,\\s*" ) ) {
					max_values.add( Integer.parseInt( e ) );
				}
				this.max_values = max_values.toArray( new Integer[ max_values.size() ] );
			}
			return this;
		}

		public Builder exponents( Double[] exponents ) {
			this.exponents = exponents;
			return this;
		}

		public Builder exponents( String adjExp ) {
			if ( adjExp != null ) {
				ArrayList<Double> exponents = new ArrayList<>();
				for ( String e : adjExp.split( "\\s*,\\s*" ) ) {
					exponents.add( Double.parseDouble( e ) );
				}
				this.exponents = exponents.toArray( new Double[ exponents.size() ] );
			}
			return this;
		}

		public Builder quality( Float q ) {
			this.quality = q;
			return this;
		}

		public Builder dimensions( int width, int height ) {
			this.width = width;
			this.height = height;
			return this;
		}

		public Parameters build() {
			Parameters p = new Parameters();
			p.set( Parameter.COLORS, colors );
			p.set( Parameter.MINVALUES, min_values );
			p.set( Parameter.MAXVALUES, max_values );
			p.set( Parameter.EXPONENTS, exponents );
			p.set( Parameter.QUALITY, quality );
			p.set( Parameter.WIDTH, width );
			p.set( Parameter.HEIGHT, height );
			return p;
		}
	}

	public <T> void set( Parameter parameter, T value ) {
		parameters.put( parameter, value );
	}

	@SuppressWarnings( "unchecked" )
	public <T> T get( Parameter parameter ) {
		return (T) parameters.get( parameter );
	}

	public boolean has( Parameter parameter ) {
		return parameters.containsKey( parameter ) && parameters.get( parameter ) != null;
	}

	@Override
	public String toString() {
		//return Arrays.stream(parameters.values()).filter( p -> p != null ).map(Object::toString).toArray( String[]::new );
		// TODO output parameters
		return "";
		//return "[" + Arrays.stream( colors ).forEach(Color::name ) + "|" + min_values + "|" + max_values + "|" + exponents + "]";
	}
}
