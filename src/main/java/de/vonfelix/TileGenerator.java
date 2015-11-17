package de.vonfelix;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;


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
		if( ! grayMaps.containsKey( limit ) ) {
			double exp = 1;
			int target = 256;
			
			int[] grayMap = new int[65536];
			for ( int i = 0; i < 65536; ++i ) {
				if( i < limit ) {
					int c = (int)( Math.pow( ((double)i/limit), (double)exp ) * target );
					grayMap[ i ] = c;
				} else {
					// mark overexposed pixels;
					grayMap[ i ] = 0b11111111;
				}
			}
			grayMaps.put( limit, grayMap );
		}
		return grayMaps.get( limit );
	}
	
	private int[] getColormap() {
		if( colormap == null ) {
			long startTime = System.nanoTime();

			int limit =  Tileserver.hasProperty("tile_value_limit") ? Integer.parseInt( Tileserver.getProperty("tile_value_limit") ) : 65536;
			double exp = Tileserver.hasProperty("contrast_adj_exp") ? Double.parseDouble( Tileserver.getProperty("contrast_adj_exp") ) : 1;
			int target = 256;

			System.out.print( "Generating colormap with adjustment=" + exp + " and limit=" + limit );

			colormap = new int[65536];
			for( int i= 0; i < 65536; ++i ) {
				if( i < limit ) {
					int c = (int)( Math.pow( ((double)i/limit), (double)exp ) * target );
					colormap[i] = ( c << 16 ) | (c << 8 ) | c;
				}
				else {
					// mark overexposed pixels
					colormap[i] = ChannelColor.BLUE;
				}
			}
			
			// for debugging
			if( Boolean.parseBoolean( Tileserver.getProperty("debug") ) ) {
				System.out.println( "using debug colormap" );
				colormap[40000] = ChannelColor.GREEN;
				colormap[50000] = ChannelColor.RED;
				colormap[65535] = ChannelColor.CYAN;
				colormap[30000] = ChannelColor.GRAYS;
			}
			
			System.out.println( " ... done (" + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );
		}
		
		return colormap;
	}
	
	public BufferedImage getTile( Stack stack, int scaleLevel, TileCoordinates coordinates ) throws Exception {
		
		long startTime = System.nanoTime();

		final boolean debug = Boolean.parseBoolean( Tileserver.getProperty("debug") );
		final boolean debug_tile_overlap = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_overlap") );
		final boolean debug_tile_bounds = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_bounds") );

		int size = coordinates.getSize();
		
		// get flattened pixel array
		// may be smaller than size*size if out of bounds
		MDShortArray data = stack.getBlock( scaleLevel, size, coordinates.getZ(), coordinates.getX(), coordinates.getY() );
		int data_width = data.dimensions()[2];
		int data_height = data.dimensions()[1];
		
		System.out.print( "TileGenerator: Generating tile ... " );
		
		short[] flatdata = data.getAsFlatArray();
		
		// create square rgb array of width 'size'
		int[] rgb = new int[ size * size ];
		
		for(int y = 0; y < size; ++y) {
			for(int x= 0; x < size; ++x ) {

				// fill overlap with black (or grey if in debug mode)
				short fillpixel = (short) (debug_tile_overlap ? 30000 : 0 ) ; 
				
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
				
				rgb[ size*y + x ] = getColormap()[pixel & 0xffff ];
			}
		}
		
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);

		img.setRGB(0, 0, size, size, rgb, 0, size);

		System.out.println( " done (" + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );

		return img;
	}
	

	public BufferedImage getTile( CompositeStack stack, int scaleLevel, TileCoordinates coordinates ) throws Exception {

		final boolean debug = Boolean.parseBoolean( Tileserver.getProperty("debug") );
		final boolean debug_tile_overlap = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_overlap") );
		final boolean debug_tile_bounds = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_bounds") );
		
		int size = coordinates.getSize();

		// create square rgb array of width 'size'
		int[] rgb = new int[ size * size ];

		System.out.println( "TileGenerator: Generating composite tile ... " );

		// get data for all channels
		for( Stack channel : stack.getChannels().values() ) {
			MDShortArray data = channel.getBlock( scaleLevel, size, coordinates.getZ(), coordinates.getX(), coordinates.getY() );
			int data_width = data.dimensions()[2];
			int data_height = data.dimensions()[1];
			
			short[] flatdata = data.getAsFlatArray();
			
			System.out.println( "  Channel " + channel.getName() + ": " + stack.getColor( channel.getName() ) );
			
			int colorMask = ChannelColor.getColor( stack.getColor( channel.getName() ) );
			int[] grayMap = getGrayMap( channel.getValueLimit() );
			
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

//		System.out.println( " done (" + ( System.nanoTime() - startTime ) / 1000000 + "ms)" );

		return img;
	}
	
//	public byte[] getTileAsByteArray( IStack stack, int scaleLevel, TileCoordinates coordinates ) throws Exception {
//		return ((DataBufferByte) getTile( stack, scaleLevel, coordinates ).getRaster().getDataBuffer()).getData();
//	}
	
	public byte[] getTileAsJPEG( IStack stack, int scaleLevel, TileCoordinates coordinates ) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//if( stack instanceof Stack ) {
		switch ( stack.getClass().getSimpleName() ) {
		case "Stack":
			ImageIO.write( getTile( (Stack)stack, scaleLevel, coordinates ), "jpg", baos);
			break;
		case "CompositeStack":
			ImageIO.write( getTile( (CompositeStack)stack, scaleLevel, coordinates ), "jpg", baos);
		default:
			// TODO throw exception
			break;
		}
		return baos.toByteArray();
	}
	
}
