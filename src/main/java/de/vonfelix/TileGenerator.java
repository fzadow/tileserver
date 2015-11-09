package de.vonfelix;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class TileGenerator {
	
	HDF5Image hdf5Image;
	int[] colormap;

	public TileGenerator( HDF5Image hdf5Image ) {
		this.hdf5Image= hdf5Image;
	}
	
	private int[] getColormap() {
		if( colormap == null ) {
			int FACTOR = 8;
			colormap = new int[65536];
			for(int i = 0; i < 65536; ++i) {
				colormap[i] = ( i / FACTOR ) << 16 | ( i / FACTOR ) << 8 | ( i / FACTOR );
			}

		}
		
		return colormap;
	}
	
	public BufferedImage getTile( Stack stack, TileCoordinates coordinates ) throws Exception {
		
		int size = coordinates.getSize();
		
		short[] data = stack.getBlock( size, coordinates.getZ(), coordinates.getX(), coordinates.getY() );
		
		int[] rgb = new int[ data.length ];
		
		for(int i = 1; i < rgb.length; ++i) {
			rgb[i] = getColormap()[data[i] & 0xffff ];
		}
		
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
//				System.out.print( String.format("%24s", Integer.toBinaryString(rgb[8*x + y])).replace(' ', '0') + "\t" );
			}
//			System.out.println();	
		}

		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);

		img.setRGB(0, 0, size, size, rgb, 0, size);
		
		return img;
	}
	
	public byte[] getTileAsByteArray( Stack stack, TileCoordinates coordinates ) throws Exception {
		return ((DataBufferByte) getTile( stack, coordinates ).getRaster().getDataBuffer()).getData();
	}
	
	public byte[] getTileAsJPEG( Stack stack, TileCoordinates coordinates ) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( getTile( stack, coordinates ), "jpg", baos);
		return baos.toByteArray();
	}
	
}
