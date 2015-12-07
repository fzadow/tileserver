package de.vonfelix.tileserver;

import java.io.File;
import java.util.HashMap;

/**
 * This class handles loading and returning of {@link AbstractImage}s
 * 
 * @author felix
 *
 */
public class ImageHandler {

	HashMap<String, AbstractImage> images = new HashMap<>();
	
	public AbstractImage getImage( String name ) {

		Tileserver.log( "getting image " + name );

		if( ! images.containsKey( name ) ||
				( Boolean.parseBoolean( Tileserver.getProperty( "debug" ) ) &&
				Boolean.parseBoolean( Tileserver.getProperty( "debug_reload_image" ) ) ) ) {
			Tileserver.log( "not loaded before" );
			if( ! new File( Tileserver.getProperty("source_image_dir") + name + ".h5" ).exists() ) {
				Tileserver.log( "Error! File " + Tileserver.getProperty( "source_image_dir" ) + name + ".h5" + " does not exist." );
				return null;
			}
			Tileserver.log( "loading " + name + "" );

			//
			// assume for now that all Images come from a HDF5 source
			//
			images.put( name, new HDF5Image( name ) );
			Tileserver.log( "File " + name + " loaded." );
		}

		return images.get( name );
	}
}
