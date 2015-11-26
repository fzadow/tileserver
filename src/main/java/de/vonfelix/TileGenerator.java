package de.vonfelix;

import java.awt.image.BufferedImage;
import java.util.HashMap;

// TODO add simple downscaling algorithm (in case scale level is not available)

// TODO different contrast curves?

// TODO automatic contrast

public class TileGenerator {
	
	int[] colormap;
	HashMap<Integer,int[]> grayMaps = new HashMap<Integer,int[]>();

	public TileGenerator() {
	}
	
	/**
	 * return a gray map that maps pixel values from 0..65535 to 0..limit. if a
	 * map for a specific limit value has not been generated before, generate
	 * it.
	 * 
	 * @param limit
	 * @return gray map for that limit
	 */
	private int[] getGrayMap( int limit ) {
		if( ! grayMaps.containsKey( limit ) || ( Boolean.parseBoolean( Tileserver.getProperty( "debug" ) ) && Boolean.parseBoolean( Tileserver.getProperty( "debug_regenerate_colormap" ) ) ) ) {
			long startTime = System.nanoTime();

			// exponent for the adjustment curve, default 1
			double exp = Tileserver.hasProperty("contrast_adj_exp") ? Double.parseDouble( Tileserver.getProperty("contrast_adj_exp") ) : 1;

			// map to 8 bit
			int target = 256;

			int[] grayMap = new int[65536];
			for ( int i = 0; i < 65536; ++i ) {
				if( i < limit ) {
					int c = (int)( Math.pow( ((double)i/limit), (double)exp ) * target );
					grayMap[ i ] = c;
				} else {
					// overexposed pixels;
					grayMap[ i ] = 0b11111111;
				}
			}
			grayMaps.put( limit, grayMap );

			Tileserver.log( "new colormap with adjustment=" + exp + " and limit=" + limit + " (" + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );
		}
		return grayMaps.get( limit );
	}
	
	/**
	 * get a single channel tile
	 * @param simpleStack
	 * @param scaleLevel
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public BufferedImage getTile( SimpleStack simpleStack, TileCoordinates coordinates ) throws Exception {
		
		long startTime = System.nanoTime();

		final boolean debug = Boolean.parseBoolean( Tileserver.getProperty("debug") );
		final boolean debug_tile_overlap = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_overlap") );
		final boolean debug_tile_bounds = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_bounds") );

		int size = coordinates.getSize();
		
		// get tile as defined by coordinates. returned tile may be smaller
		// than size*size if it overlaps the bounds of the image.
		Tile tile = simpleStack.getTile( coordinates );
		int tile_width = tile.getWidth();
		int tile_height = tile.getHeight();
		short[] flatdata = tile.getTile();
		
		// create square rgb array of width 'size'
		int[] rgb = new int[ size * size ];

		// fill overlap with black (or grey if in debug mode)
		short fillpixel = (short) (debug_tile_overlap ? 30000 : 0 ) ; 

		int[] grayMap = getGrayMap( simpleStack.getValueLimit() );
		Tileserver.log( "getting grayscale tile " + simpleStack + ", limit=" + simpleStack.getValueLimit() );

		for(int y = 0; y < size; ++y) {
			for(int x= 0; x < size; ++x ) {

				short pixel = x >= tile_width || y >= tile_height ? fillpixel : flatdata[ tile_width * y + x ];

				if ( debug_tile_bounds ) {
					if( ( x % 64 == 0 && y % 64 == 0 ) || ( (x-1) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && (y-1) % 64 == 0 ) || ( (x+1) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && (y+1) % 64 == 0 ) || ( (x-2) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && (y-2) % 64 == 0 ) || ( (x+2) % 64 == 0 && y % 64 == 0 ) || ( x % 64 == 0 && (y+2) % 64 == 0 ) ) {
						pixel = (short)30000;
					}
					if( ( x == 0 && y % 4 == 0 ) || ( y == 0 && x % 4 == 0 ) ) {
						pixel = (short)40000;
					}
					if( ( x == size-1 && y % 4 == 0 ) || ( y == size-1 && x % 4 == 0 ) ) {
						pixel = (short)50000;
					}
				}
				int grayValue = grayMap[pixel & 0xffff ];

				rgb[ size*y + x ] = grayValue << 16 | grayValue << 8 | grayValue;
			}
		}
		
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);

		img.setRGB(0, 0, size, size, rgb, 0, size);

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
	public BufferedImage getTile( CompositeStack compositeStack, TileCoordinates coordinates ) throws Exception {

		long startTime = System.nanoTime();

		final boolean debug = Boolean.parseBoolean( Tileserver.getProperty("debug") );
		final boolean debug_tile_overlap = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_overlap") );
		final boolean debug_tile_bounds = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_bounds") );
		
		int size = coordinates.getSize();

		// create square rgb array of width 'size'
		int[] rgb = new int[ size * size ];

		Tileserver.log( "getting composite tile " + compositeStack + ", limit=" + compositeStack.getValueLimit() );

		// get data for all channels
		for( Channel channel : compositeStack.channels() ) {
			Tile tile = channel.getStack().getTile( coordinates );
			int tile_width = tile.getWidth();
			int tile_height = tile.getHeight();
			short[] flatdata = tile.getTile();
			
			int colorMask = channel.getColorValue();
			int[] grayMap = getGrayMap( channel.getValueLimit() );

			Tileserver.log( "  channel " + channel + ", limit=" + channel.getStack().getValueLimit() );

			for(int y = 0; y < size; ++y) {
				for(int x= 0; x < size; ++x ) {
					// fill overlap with black
					short pixel = x >= tile_width || y >= tile_height ? 0 : flatdata[ tile_width * y + x ];
					int grayValue = grayMap[pixel & 0xffff ];
					rgb[ size*y + x ] = rgb[ size*y + x ] | ( ( grayValue << 16 ) | (grayValue << 8 ) | grayValue ) & colorMask;
				}
			}
		}
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);

		img.setRGB(0, 0, size, size, rgb, 0, size);

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
	public BufferedImage getTile( IStack stack, TileCoordinates coordinates ) throws Exception {
		BufferedImage image;
		switch ( stack.getClass().getSimpleName() ) {
		case "Stack":
			image = getTile( (SimpleStack)stack, coordinates );
			break;
		case "CompositeStack":
			image = getTile( (CompositeStack)stack, coordinates );
			break;
		default:
			return null;
		}
		return image;
	}
	
}
