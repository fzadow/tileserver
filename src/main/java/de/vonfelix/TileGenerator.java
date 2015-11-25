package de.vonfelix;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import ch.systemsx.cisd.base.mdarray.MDShortArray;

// TODO add simple downscaling algorithm (in case scale level is not available)

// TODO different contrast curves?

// TODO automatic contrast

public class TileGenerator {
	
	int[] colormap;
	HashMap<Integer,int[]> grayMaps = new HashMap<Integer,int[]>();

	public TileGenerator() {
	}
	
	private int[] getGrayMap( int limit ) {
		if( ! grayMaps.containsKey( limit ) || ( Boolean.parseBoolean( Tileserver.getProperty( "debug" ) ) && Boolean.parseBoolean( Tileserver.getProperty( "debug_regenerate_colormap" ) ) ) ) {
			long startTime = System.nanoTime();

			double exp = Tileserver.hasProperty("contrast_adj_exp") ? Double.parseDouble( Tileserver.getProperty("contrast_adj_exp") ) : 1;
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

			// Tileserver.log( "new colormap with adjustment=" + exp + " and
			// limit=" + limit + " (" + ( System.nanoTime() - startTime ) /
			// 1000000 + "ms)" );
		}
		return grayMaps.get( limit );
	}
	
	/**
	 * get a single channel tile
	 * @param stack
	 * @param scaleLevel
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public BufferedImage getTile( Stack stack, TileCoordinates coordinates ) throws Exception {
		
		long startTime = System.nanoTime();

		final boolean debug = Boolean.parseBoolean( Tileserver.getProperty("debug") );
		final boolean debug_tile_overlap = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_overlap") );
		final boolean debug_tile_bounds = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_bounds") );

		int size = coordinates.getSize();
		
		// get flattened pixel array
		// may be smaller than size*size if out of bounds
		MDShortArray data = stack.getBlock( coordinates.getScaleLevel(), size, coordinates.getZ(), coordinates.getX(), coordinates.getY() );
		int data_width = data.dimensions()[2];
		int data_height = data.dimensions()[1];
		
		short[] flatdata = data.getAsFlatArray();
		
		// create square rgb array of width 'size'
		int[] rgb = new int[ size * size ];

		// fill overlap with black (or grey if in debug mode)
		short fillpixel = (short) (debug_tile_overlap ? 30000 : 0 ) ; 

		int[] grayMap = getGrayMap( stack.getValueLimit() );
		Tileserver.log( "getting grayscale tile " + stack + ", limit=" + stack.getValueLimit() );

		for(int y = 0; y < size; ++y) {
			for(int x= 0; x < size; ++x ) {

				short pixel = x >= data_width || y >= data_height ? fillpixel : flatdata[ data_width*y + x ];

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
	 * get a composite tile
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
			MDShortArray data = channel.getStack().getBlock( coordinates.getScaleLevel(), size, coordinates.getZ(), coordinates.getX(), coordinates.getY() );
			int data_width = data.dimensions()[2];
			int data_height = data.dimensions()[1];
			
			short[] flatdata = data.getAsFlatArray();
			
			int colorMask = channel.getColorValue();
			int[] grayMap = getGrayMap( channel.getStack().getValueLimit() );

			Tileserver.log( "  channel " + channel + ", limit=" + channel.getStack().getValueLimit() );

			for(int y = 0; y < size; ++y) {
				for(int x= 0; x < size; ++x ) {
					// fill overlap with black
					short pixel = x >= data_width || y >= data_height ? 0 : flatdata[ data_width*y + x ];
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
	
	public BufferedImage getTile( IStack stack, TileCoordinates coordinates ) throws Exception {
		BufferedImage image;
		switch ( stack.getClass().getSimpleName() ) {
		case "Stack":
			image = getTile( (Stack)stack, coordinates );
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
