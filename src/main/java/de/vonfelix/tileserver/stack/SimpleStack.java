package de.vonfelix.tileserver.stack;

import de.vonfelix.tileserver.image.AbstractImage;
import de.vonfelix.tileserver.tile.TilePixels;
import de.vonfelix.tileserver.tile.Coordinates;

/**
 * A single Stack
 * @author felix
 *
 */
public abstract class SimpleStack extends AbstractStack {
	
	/**
	 * 
	 * @param hdf5Image reference to the HDF5Image containing the Image
	 * @param id Name to identify the Stack within the HDF5 file
	 */
	public SimpleStack( AbstractImage image, String id, String title ) {
		super( image, id, title );
	}

	public abstract TilePixels getTilePixels( Coordinates coordinates );

}
