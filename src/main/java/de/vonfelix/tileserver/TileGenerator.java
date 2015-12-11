package de.vonfelix.tileserver;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.vonfelix.tileserver.exception.TileOutOfBoundsException;

// TODO add simple downscaling algorithm (in case scale level is not available)

// TODO automatic contrast

public class TileGenerator {

	static Logger logger = LogManager.getLogger();

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

		if ( !grayMaps.containsKey( key ) || ( Boolean.parseBoolean( Tileserver.getProperty( "debug" ) ) && Boolean.parseBoolean( Tileserver.getProperty( "debug_regenerate_colormap" ) ) ) ) {
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
	
	public BufferedImage getTile(
			IStack stack,
			TileCoordinates coordinates,
			TileParameters parameters ) throws Exception {

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

		Tile tile = null;
		Color color = null;
		
		for( int i= 0; i < numberOfChannels; ++i ) {
			exp = ( i < parameters.getExponents().length ) ? parameters.getExponents()[ i ] : Tileserver.hasProperty( "contrast_adj_exp" ) ? Double.parseDouble( Tileserver.getProperty( "contrast_adj_exp" ) ) : 1;

			if( composite ) {
				Channel channel = ( (CompositeStack) stack ).getChannel( i );
				tile = channel.getStack().getTile( coordinates );

				color = ( i < parameters.getColors().length ) ? parameters.getColors()[ i ] : channel.getColor();
				min = ( i < parameters.getMinValues().length ) ? parameters.getMinValues()[ i ] : 0;
				max = ( i < parameters.getMaxValues().length ) ? parameters.getMaxValues()[ i ] : channel.getValueLimit();
				logger.trace( "  channel: " + channel + ", dyn.range=" + min + ".." + max + " exp=" + exp + " color=" + color );

			}
			else {
				tile =  ( (SimpleStack) stack ).getTile( coordinates );
				color = Color.GRAYS;
				min = ( parameters.getMinValues().length > 0 ) ? parameters.getMinValues()[ i ] : 0;
				max = ( parameters.getMaxValues().length > 0 ) ? parameters.getMaxValues()[ i ] : stack.getValueLimit();
				logger.trace( "  stack: " + stack + ", dyn. range=" + min + ".." + max + " exp=" + exp );
			}

			pixels[ i ] = tile.getTile();

			rs[ i ] = color.r();
			gs[ i ] = color.g();
			bs[ i ] = color.b();

			grayMaps[ i ] = getGrayMap( min, max, exp );

		}
		

		int tile_width = tile.getWidth();
		int tile_height = tile.getHeight();

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

		BufferedImage img = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		img.setRGB( 0, 0, width, height, rgb, 0, width );

		logger.debug( "tile generated (" + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );

		return img;
	}
}
