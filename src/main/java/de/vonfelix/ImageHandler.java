package de.vonfelix;

import java.io.File;
import java.util.HashMap;

public class ImageHandler {

	HashMap<String, HDF5Image> images;
	
	public ImageHandler() {
		images = new HashMap<String, HDF5Image>();
	}
	
	public HDF5Image getImage( String name ) {
		
		if( ! images.containsKey( name ) ||
				( Boolean.parseBoolean( Tileserver.getProperty( "debug" ) ) &&
				Boolean.parseBoolean( Tileserver.getProperty( "debug_reload_image" ) ) ) ) {
			if( ! new File( Tileserver.getProperty("source_image_dir") + name + ".h5" ).exists() ) {
				System.out.println( "ImageHandler: Error! File " + Tileserver.getProperty("source_image_dir") + name + ".h5" + " does not exist." );
				return null;
			}
			images.put( name, new HDF5Image( name ) );
			System.out.println( "ImageHandler: File " + name + " loaded." );
		}

		return images.get( name );
	}
}
