package de.vonfelix.tileserver.tile;

import static de.vonfelix.tileserver.tile.Parameters.Parameter.*;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

import de.vonfelix.tileserver.Color;
import de.vonfelix.tileserver.exception.TileOutOfBoundsException;
import de.vonfelix.tileserver.stack.Channel;
import de.vonfelix.tileserver.stack.CompositeStack;
import de.vonfelix.tileserver.stack.IStack;
import de.vonfelix.tileserver.stack.SimpleStack;

// TODO add simple downscaling algorithm (in case scale level is not available)

// TODO automatic contrast

public class TileGenerator {

	private Environment env;

	static Logger logger = LogManager.getLogger();

	public TileGenerator( Environment env ) {
		this.env = env;
	}

	/*
	 * create a custom LinkedHashMap that holds 40 entries (~10 MB) and discards the last recently
	 * used one when full
	 */
	LinkedHashMap<String, int[]> grayMaps = new LinkedHashMap<String, int[]>( 40, 1f, true) {
		private static final long serialVersionUID = 560852434281381905L;

		@Override
		protected boolean removeEldestEntry( java.util.Map.Entry<String, int[]> eldest ) {
			return size( ) > 40;
		}
	};

	/**
	 * return a gray map that maps pixel values from 0..65535 to min..max.
	 * 
	 * The 40 most recent maps (about 10 MB) are kept in memory. If a map is not
	 * in memory, it will be generated.
	 * 
	 * @param min
	 * @param max
	 * @param exp
	 *            Exponent for the mapping (1 = linar)
	 * @return gray map for that limit
	 */
	private int[] getGrayMap( int min, int max, double exp ) {

		// key to identify map
		String key = min + ":" + max + ":" + exp;

		if (!grayMaps.containsKey(key) || (env.getProperty("tilebuilder.debug.enabled", Boolean.class)
				&& env.getProperty("tilebuilder.debug.regenerate_colormap", Boolean.class))) {
			long startTime = System.nanoTime();

			// map to 8 bit
			int target = 256;

			int[] grayMap = new int[65536];
			for ( int i = 0; i < 65536; ++i ) {
				// no need to check for i < min, primitive array values are
				// already 0 anyway.
				if ( i >= min && i < max ) {
					int c = (int) ( Math.pow( ( (double) i / max ), (double) exp ) * target );
					grayMap[ i ] = c;
				} else {
					// overexposed pixels;
					grayMap[ i ] = 0b11111111;
				}
			}
			grayMaps.put( key, grayMap );

			logger.trace( "generated new colormap with adjustment=" + exp + " and range=" + min + "-" + max + " (took " + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );
		}
		return grayMaps.get( key );
	}
	
	public Tile getTile(
			IStack stack,
			Coordinates coordinates,
			Parameters parameters ) throws Exception {

		long startTime = System.nanoTime();

		boolean composite = false;

		// check if requested tile is out of bounds
		int scaleLevel = coordinates.getScaleLevel();
		if ( coordinates.getX() >= stack.getWidth( scaleLevel ) || coordinates.getY() >= stack.getHeight( scaleLevel ) || coordinates.getZ() >= stack.getDepth( scaleLevel ) ) {
			throw new TileOutOfBoundsException( stack, coordinates );
		}

		int width = coordinates.getWidth();
		int height = coordinates.getHeight();

		// create rgb array
		int[] rgb = new int[ width * height ];

		if ( stack.getClass().getSimpleName().equals( "CompositeStack" ) ) {
			composite = true;
		}

		logger.debug( "generating " + ( composite ? "composite " : "" ) + "tile for " + stack + " at " + coordinates );

		int numberOfChannels = composite ? ( (CompositeStack) stack ).numberOfChannels() : 1;

		short[][] pixels = new short[ numberOfChannels ][];
		int[] rs = new int[ numberOfChannels ];
		int[] gs = new int[ numberOfChannels ];
		int[] bs = new int[ numberOfChannels ];
		int[][] grayMaps = new int[ numberOfChannels ][];
		int min, max;
		double exp;

		TilePixels tilePixels = null;
		Color color = null;
		
		for( int i= 0; i < numberOfChannels; ++i ) {
			exp = ( parameters.has( EXPONENTS ) && i < parameters.<Double[]> get( EXPONENTS ).length )
					? parameters.<Double[]> get( EXPONENTS )[ i ]
					: env.getProperty( "tilebuilder.gamma", Double.class, 1d );

			if( composite ) {
				Channel channel = ( (CompositeStack) stack ).getChannel( i );
				tilePixels = channel.getStack().getTilePixels( coordinates );

				color = ( parameters.has( COLORS ) && i < parameters.<Color[]> get( COLORS ).length ) ? parameters.<Color[]> get( COLORS )[ i ] : channel.getColor();
				min = ( parameters.has( MINVALUES ) && i < parameters.<Integer[]> get( MINVALUES ).length ) ? parameters.<Integer[]> get( MINVALUES )[ i ] : 0;
				max = ( parameters.has( MAXVALUES ) && i < parameters.<Integer[]> get( MAXVALUES ).length )
						? parameters.<Integer[]> get( MAXVALUES )[ i ] : channel.getMax();
				logger.trace( "  channel: " + channel + ", dyn.range=" + min + ".." + max + " exp=" + exp + " color=" + color );

			}
			else {
				tilePixels =  ( (SimpleStack) stack ).getTilePixels( coordinates );
				color = Color.GRAYS;
				min = ( parameters.has( MINVALUES ) && parameters.<Integer[]> get( MINVALUES ).length > 0 ) ? parameters.<Integer[]> get( MINVALUES )[ i ] : 0;
				max = ( parameters.has( MAXVALUES ) && parameters.<Integer[]> get( MAXVALUES ).length > 0 )
						? parameters.<Integer[]> get( MAXVALUES )[ i ] : stack.getMax();
				logger.trace( "  stack: " + stack + ", dyn. range=" + min + ".." + max + " exp=" + exp );
			}

			pixels[ i ] = tilePixels.getTile();

			rs[ i ] = color.r();
			gs[ i ] = color.g();
			bs[ i ] = color.b();

			grayMaps[ i ] = getGrayMap( min, max, exp );

		}
		

		int tile_width = tilePixels.getWidth();
		int tile_height = tilePixels.getHeight();

		int r, g, b;
		int grayValue;
		int pixel_index;
		int row_index;

		for ( int y = 0; y < tile_height; ++y ) {
			row_index = tile_width * y;
			for ( int x = 0; x < tile_width; ++x ) {
				r = g = b = 0;
				pixel_index = row_index + x;
				for ( int c = 0; c < numberOfChannels; ++c ) {
					grayValue = grayMaps[ c ][ pixels[ c ][ pixel_index ] & 0xffff ];

					// ADD blend mode
					r += grayValue & rs[ c ];
					g += grayValue & gs[ c ];
					b += grayValue & bs[ c ];
				}
				r = Math.min( r, 255 );
				g = Math.min( g, 255 );
				b = Math.min( b, 255 );
				rgb[ width * y + x ] = r << 16 | g << 8 | b;
				//img.setRGB( x, y, r << 16 | g << 8 | b );
			}
		}

		// Show tile overlap if debugging enabled
		if (env.getProperty("tilebuilder.debug.enabled", Boolean.class, false)
				&& env.getProperty("tilebuilder.debug.tile.overlap", Boolean.class, false)) {
			if (tile_width < width || tile_height < height) {
				for (int y = 0; y < height; ++y) {
					for (int x = 0; x < width; ++x) {
						if (x > tile_width || y > tile_height) {
							rgb[width * y + x] = 255 << 16 | 255 << 8 | 127;
						}
					}
				}
			}
		}

		// Show Border if debugging enabled
		if (env.getProperty("tilebuilder.debug.enabled", Boolean.class, false)
				&& env.getProperty("tilebuilder.debug.tile.bounds", Boolean.class, false)) {
			for (int i = 0; i < width; ++i) {
				rgb[i] = 255 << 16 | 0 << 8 | 0; // top
				rgb[width * i] = 0 << 16 | 255 << 8 | 255; // left
				rgb[width * i + width - 1] = 255 << 16 | 0 << 8 | 255; // right
				rgb[i + (width - 1) * (width - 1)] = 0 << 16 | 255 << 8 | 0; // bottom
			}
		}

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		img.setRGB(0, 0, width, height, rgb, 0, width);

		logger.debug( "tile generated (" + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );

		return new Tile( img, coordinates, parameters );
	}
}
