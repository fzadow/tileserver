package de.vonfelix.tileserver;

import java.io.File;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.vonfelix.tileserver.exception.ImageNotFoundException;

/**
 * This class handles loading and returning of {@link AbstractImage}s
 * 
 * @author felix
 *
 */
public class ImageHandler {

	static Logger logger = LogManager.getLogger();

	HashMap<String, AbstractImage> images = new HashMap<>();
	
	public AbstractImage getImage( String name ) {

		logger.debug( "getting image " + name );

		if( ! images.containsKey( name ) ||
				( Boolean.parseBoolean( Tileserver.getProperty( "debug" ) ) &&
				Boolean.parseBoolean( Tileserver.getProperty( "debug_reload_image" ) ) ) ) {
			if( ! new File( Tileserver.getProperty("source_image_dir") + name + ".h5" ).exists() ) {
				throw new ImageNotFoundException( name );
			}
			logger.info( "loading image " + name );

			//
			// assume for now that all Images come from a HDF5 source
			//
			images.put( name, new HDF5Image( name ) );
		}

		return images.get( name );
	}
}
