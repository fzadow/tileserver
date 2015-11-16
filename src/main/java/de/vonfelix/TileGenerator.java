package de.vonfelix;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.imageio.ImageIO;


import ch.systemsx.cisd.base.mdarray.MDShortArray;

// TODO add simple downscaling algorithm (in case scale level is not available)

// TODO different contrast curves?

// TODO automatic contrast

public class TileGenerator {
	
	int[] colormap;

	public TileGenerator() {
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
					colormap[i] = 0b111111110000000011111111;
				}
			}
			
			// for debugging
			if( Boolean.parseBoolean( Tileserver.getProperty("debug") ) ) {
				System.out.println( "using debug colormap" );
				colormap[40000] = 0b000000001111111100000000; // green
				colormap[50000] = 0b111111110000000000000000; // red
				colormap[65535] = 0b000000001111111111111111; // cyan
				colormap[30000] = 0b001111110011111100111111; // grey
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
	

	// TODO Work in progress
	public BufferedImage getTile( CompositeStack stack, int scaleLevel, TileCoordinates coordinates ) throws Exception {

		final boolean debug = Boolean.parseBoolean( Tileserver.getProperty("debug") );
		final boolean debug_tile_overlap = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_overlap") );
		final boolean debug_tile_bounds = debug && Boolean.parseBoolean( Tileserver.getProperty("debug_tile_bounds") );
		
		int size = coordinates.getSize();
		
		// get data for all channels
		for( Stack channel : stack.getChannels().values() ) {
			MDShortArray data = channel.getBlock( scaleLevel, size, coordinates.getZ(), coordinates.getX(), coordinates.getY() );
			int data_width = data.dimensions()[2];
			int data_height = data.dimensions()[1];
			System.out.print( "TileGenerator: Generating composite tile ... " );
			
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
		}

		return null;
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
