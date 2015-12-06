package de.vonfelix;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;

import org.openjdk.jol.info.GraphLayout;

// TODO add simple downscaling algorithm (in case scale level is not available)

// TODO different contrast curves?

// TODO automatic contrast

public class TileGenerator {
	
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
				if ( i > min && i < max ) {
					int c = (int) ( Math.pow( ( (double) i / max ), (double) exp ) * target );
					grayMap[ i ] = c;
				} else {
					// overexposed pixels;
					grayMap[ i ] = 0b11111111;
				}
			}
			grayMaps.put( key, grayMap );

			Tileserver.log( "generated new colormap with adjustment=" + exp + " and range=" + min + "-" + max + " (took " + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );
			Tileserver.log( "size of all " + grayMaps.size() + " colormaps: " + ( GraphLayout.parseInstance( grayMaps ).totalSize() / 1024 ) + "KB" );
		}
		return grayMaps.get( key );
	}
	
	/**
	 * get a single channel tile
	 * @param simpleStack
	 * @param scaleLevel
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public BufferedImage getTile(
			SimpleStack simpleStack,
			TileCoordinates coordinates,
			TileParameters parameters )
			throws Exception {
		
		long startTime = System.nanoTime();

		final boolean debug = Boolean.parseBoolean( Tileserver.getProperty("debug") );
		final boolean debug_tile_overlap = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_overlap") );
		final boolean debug_tile_bounds = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_bounds") );

		int width = coordinates.getWidth();
		int height = coordinates.getHeight();
		
		// get tile as defined by coordinates. returned tile may be smaller
		// than size*size if it overlaps the bounds of the image.
		Tile tile = simpleStack.getTile( coordinates );
		int tile_width = tile.getWidth();
		int tile_height = tile.getHeight();
		short[] flatdata = tile.getTile();

		// create rgb array
		int[] rgb = new int[ width * height ];

		// fill overlap with black (or grey if in debug mode)
		short fillpixel = (short) ( debug_tile_overlap ? simpleStack.getValueLimit() / 2 : 0 );

		int min = ( parameters.getMinValues().length > 0 ) ? parameters.getMinValues()[ 0 ] : 0;
		int max = ( parameters.getMaxValues().length > 0 ) ? parameters.getMaxValues()[ 0 ] : simpleStack.getValueLimit();
		double exp = ( parameters.getExponents().length > 0 ) ? parameters.getExponents()[ 0 ] : Tileserver.hasProperty( "contrast_adj_exp" ) ? Double.parseDouble( Tileserver.getProperty( "contrast_adj_exp" ) ) : 1;
		
		int[] grayMap = getGrayMap( min, max, exp );

		Tileserver.log( "getting grayscale tile " + simpleStack + ", range " + min + "-" + max + ", exp " + exp );

		for ( int y = 0; y < height; ++y ) {
			for ( int x = 0; x < width; ++x ) {

				short pixel = x >= tile_width || y >= tile_height ? fillpixel : flatdata[ tile_width * y + x ];

				if ( debug_tile_bounds ) {
					if ( ( x % 64 == 0 && y % 64 == 0 ) || ( ( x - 1 ) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && ( y - 1 ) % 64 == 0 ) || ( ( x + 1 ) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && ( y + 1 ) % 64 == 0 ) || ( ( x - 2 ) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && ( y - 2 ) % 64 == 0 ) || ( ( x + 2 ) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && ( y + 2 ) % 64 == 0 ) )
						pixel = (short)30000;
					if ( ( x == 0 && y % 4 == 0 ) || ( y == 0 && x % 4 == 0 ) )
						pixel = (short)40000;
					if ( ( x == width - 1 && y % 4 == 0 ) || ( y == height - 1 && x % 4 == 0 ) )
						pixel = (short)50000;
				}
				int grayValue = grayMap[pixel & 0xffff ];

				rgb[ width * y + x ] = grayValue << 16 | grayValue << 8 | grayValue;
			}
		}
		
		BufferedImage img = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );

		img.setRGB( 0, 0, width, height, rgb, 0, width );

		Tileserver.log( "tile generated (" + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );

		return img;
	}
	

	/**
	 * create a composite tile by reading all component channels and assembling
	 * them according to their colors
	 * 
	 * @param compositeStack
	 * @param scaleLevel
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public BufferedImage getTile(
			CompositeStack compositeStack,
			TileCoordinates coordinates,
			TileParameters parameters ) throws Exception {

		long startTime = System.nanoTime();

		final boolean debug = Boolean.parseBoolean( Tileserver.getProperty("debug") );
		final boolean debug_tile_overlap = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_overlap") );
		final boolean debug_tile_bounds = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_bounds") );
		
		int width = coordinates.getWidth();
		int height = coordinates.getHeight();

		// create square rgb array of width 'size'
		int[] rgb = new int[ width * height ];

		// fill overlap with black (or grey if in debug mode)
		short fillpixel = (short) ( debug_tile_overlap ? compositeStack.getValueLimit() / 2 : 0 );

		Tileserver.log( "getting composite tile " + compositeStack + ", limit=" + compositeStack.getValueLimit() );

		int c = -1;
		// get data for all channels
		for( Channel channel : compositeStack.channels() ) {
			c++;

			Tile tile = channel.getStack().getTile( coordinates );
			int tile_width = tile.getWidth();
			int tile_height = tile.getHeight();
			short[] flatdata = tile.getTile();
			
			int colorMask = ( c < parameters.getColors().length ) ? parameters.getColors()[ c ].value() : channel.getColor().value();

			int min = ( parameters.getMinValues().length > 0 ) ? parameters.getMinValues()[ c ] : 0;
			int max = ( parameters.getMaxValues().length > 0 ) ? parameters.getMaxValues()[ c ] : channel.getValueLimit();
			double exp = ( parameters.getExponents().length > 0 ) ? parameters.getExponents()[ c ] : Tileserver.hasProperty( "contrast_adj_exp" ) ? Double.parseDouble( Tileserver.getProperty( "contrast_adj_exp" ) ) : 1;

			int[] grayMap = getGrayMap( min, max, exp );

			Tileserver.log( "  channel " + channel );

			for ( int y = 0; y < height; ++y ) {
				for ( int x = 0; x < width; ++x ) {
					short pixel = x >= tile_width || y >= tile_height ? fillpixel : flatdata[ tile_width * y + x ];
					if ( debug_tile_bounds ) {
						if ( ( x % 64 == 0 && y % 64 == 0 ) || ( ( x - 1 ) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && ( y - 1 ) % 64 == 0 ) || ( ( x + 1 ) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && ( y + 1 ) % 64 == 0 ) || ( ( x - 2 ) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && ( y - 2 ) % 64 == 0 ) || ( ( x + 2 ) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && ( y + 2 ) % 64 == 0 ) )
							pixel = (short) 30000;
						if ( ( x == 0 && y % 4 == 0 ) || ( y == 0 && x % 4 == 0 ) )
							pixel = (short) 40000;
						if ( ( x == width - 1 && y % 4 == 0 ) || ( y == height - 1 && x % 4 == 0 ) )
							pixel = (short) 50000;
					}

					int grayValue = grayMap[pixel & 0xffff ];
					rgb[ width * y + x ] = rgb[ width * y + x ] | ( ( grayValue << 16 ) | ( grayValue << 8 ) | grayValue ) & colorMask;
				}
			}
		}
		BufferedImage img = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );

		img.setRGB( 0, 0, width, height, rgb, 0, width );

		Tileserver.log( "composite tile generated (" + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );

		return img;
	}
	
//	public byte[] getTileAsByteArray( IStack stack, int scaleLevel, TileCoordinates coordinates ) throws Exception {
//		return ((DataBufferByte) getTile( stack, scaleLevel, coordinates ).getRaster().getDataBuffer()).getData();
//	}
	
	/**
	 * generates a BufferedImage tile
	 * 
	 * @param stack
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public BufferedImage getTile( IStack stack, TileCoordinates coordinates, TileParameters parameters )
			throws Exception {
		BufferedImage image;
		switch ( stack.getClass().getSimpleName() ) {
		case "CompositeStack":
			image = getTile( (CompositeStack) stack, coordinates, parameters );
			break;
		case "Stack":
		case "HDF5Stack":
			image = getTile( (SimpleStack) stack, coordinates, parameters );
			break;
		default:
			return null;
		}
		return image;
	}
	
}
