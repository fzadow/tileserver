package de.vonfelix;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class HDF5Test
{
	public static void main( final String[] args ) throws IOException
	{
		String FILENAME = "/home/felix/Dev/tileserver/ovary.h5";
		
		HDF5Image hdf5Image;
		try {
			hdf5Image = new HDF5Image( FILENAME );

			System.out.println("Image loaded (" + FILENAME + ")");
			System.out.println("Number of Channels: " + hdf5Image.getNumChannels() );
			
			System.out.println( "Channel 0: " + hdf5Image.getChannel(0) );
			
			TileGenerator tileGenerator = new TileGenerator(hdf5Image);
			
			BufferedImage img = tileGenerator.getTile( 0, new TileCoordinates(hdf5Image, 128, 0, 0, 0) );
			ImageIO.write( img, "jpg", new File( "img.jpg" ) );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//WritableRaster raster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE, 256, 256, 1, 256, null);
		//System.out.println( raster.getWidth() );
		
		
		//Raster
		
		//BufferedImage
		
		//ImageJ.main( args );
		//ImageJFunctions.show(img);
//		ImageJFunctions.show( ArrayImgs.unsignedShorts( data, new long[] { SIZE, SIZE } ) );
	}
}
