package de.vonfelix;

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

	public abstract Tile getTile( TileCoordinates coordinates );

}
