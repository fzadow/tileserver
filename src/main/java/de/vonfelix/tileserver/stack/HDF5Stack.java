package de.vonfelix.tileserver.stack;

import ch.systemsx.cisd.base.mdarray.MDShortArray;
import de.vonfelix.tileserver.image.HDF5YAMLImage;
import de.vonfelix.tileserver.tile.Coordinates;
import de.vonfelix.tileserver.tile.TilePixels;
import ncsa.hdf.hdf5lib.exceptions.HDF5JavaException;

public class HDF5Stack extends SimpleStack {

	String path;

	public HDF5Stack( HDF5YAMLImage image, String path, String id, String title ) {
		super( image, id, title );

		this.path = path;
		// this.image = (HDF5Image) image;
		scaleLevels = (int) image.getReader().object().getAllGroupMembers( path + id + "/" ).size();

		for ( int i = 0; i < scaleLevels; i++ ) {
			dimensions.put( i, ( (HDF5YAMLImage) image ).getReader().object().getDimensions( path + id + "/" + i ) );
		}
	}

	public String getPath() {
		return path;
	}

	public String getFullName() {
		return getPath() + "/" + getId();
	}

	public TilePixels getTilePixels( Coordinates coordinates ) throws HDF5JavaException {
		long offset_x = coordinates.getX();
		long offset_y = coordinates.getY();
		long offset_z = coordinates.getZ();
		int scaleLevel = coordinates.getScaleLevel();

		// restrict block loading to image bounds
		int width = offset_x + coordinates.getWidth() > getDimensions( scaleLevel )[ 2 ] ? (int) ( getDimensions( scaleLevel )[ 2 ] - offset_x - 1 ) : coordinates.getWidth();
		int height = offset_y + coordinates.getHeight() > getDimensions( scaleLevel )[ 1 ] ? (int) ( getDimensions( scaleLevel )[ 1 ] - offset_y - 1 ) : coordinates.getHeight();

		MDShortArray i = ( (HDF5YAMLImage) image ).getReader().uint16().readMDArrayBlockWithOffset( getFullName() + "/" + scaleLevel, new int[] { 1, height, width }, new long[] { offset_z, offset_y, offset_x } );

		return new TilePixels( i.getAsFlatArray(), width, height );
	}


}
