package de.vonfelix;

import java.util.List;

import ch.systemsx.cisd.base.mdarray.MDShortArray;

public class HDF5Stack extends SimpleStack {

	String path;
	List<String> scaleLevels;
	HDF5Image image;

	public HDF5Stack( HDF5Image image, String path, String id, String title ) {
		super( image, id, title );

		this.path = path;
		this.image = image;
		scaleLevels = image.getReader().object().getAllGroupMembers( path + id + "/" );
	}

	public String getPath() {
		return path;
	}

	public String getFullName() {
		return getPath() + "/" + getId();
	}

	public long[] getDimensions( int scaleLevel ) {

		if ( !dimensions.containsKey( scaleLevel ) ) {
			dimensions.put( scaleLevel, image.getReader().object().getDimensions( path + id + "/" + scaleLevel ) );
		}
		return dimensions.get( scaleLevel );
	}

	public Tile getTile( TileCoordinates coordinates ) {
		long offset_x = coordinates.getX();
		long offset_y = coordinates.getY();
		long offset_z = coordinates.getZ();
		int size = coordinates.getWidth();
		int scaleLevel = coordinates.getScaleLevel();

		// restrict block loading to image bounds
		int width = offset_x + size > getDimensions( scaleLevel )[ 2 ] ? (int) ( getDimensions( scaleLevel )[ 2 ] - offset_x - 1 ) : size;
		int height = offset_y + size > getDimensions( scaleLevel )[ 1 ] ? (int) ( getDimensions( scaleLevel )[ 1 ] - offset_y - 1 ) : size;
		
		//System.out.println( "Stack: getting " + width + "x" + height + " block from " + offset_x + "," + offset_y + " to "  + ( width + offset_x ) + "," + ( height + offset_y ) );
		
		MDShortArray i = image.getReader().uint16().readMDArrayBlockWithOffset( getFullName() + "/" + scaleLevel,
 new int[] { 1, height, width },
 new long[] { offset_z, offset_y, offset_x } );

		return new Tile( i.getAsFlatArray(), width, height );
	}


}
