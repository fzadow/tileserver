package de.vonfelix.tileserver;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class HDF5Test
{
	public static void main( final String[] args ) throws IOException
	{
		String FILENAME = "R2_disc2";
		
		HDF5Image hdf5Image;
		try {
			hdf5Image = new HDF5Image( FILENAME );

			System.out.println("Image loaded (" + FILENAME + ")");
			System.out.println("Number of Stacks: " + hdf5Image.getNumStacks() );
			
			TileGenerator tileGenerator = new TileGenerator();
			
			BufferedImage img = tileGenerator.getTile( (CompositeStack) hdf5Image.getStack( "composite1" ), new TileCoordinates( 512, 0, 1, 0, 0 ), new TileParameters( null, null, null, null ) );
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