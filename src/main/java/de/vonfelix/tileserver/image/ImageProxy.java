package de.vonfelix.tileserver.image;

import java.io.File;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.vonfelix.tileserver.Tileserver;
import de.vonfelix.tileserver.exception.ImageNotFoundException;

/**
 * This class handles loading and returning of {@link AbstractImage}s
 * 
 * @author felix
 *
 */
public class ImageProxy {

	static Logger logger = LogManager.getLogger();

	private static ImageProxy instance;

	private ImageProxy() {
	}

	public static synchronized ImageProxy getInstance() {
		if ( ImageProxy.instance == null ) {
			ImageProxy.instance = new ImageProxy();
			logger.info( "ImageProxy initialized. Source directory: " + Tileserver.getProperty( "source_image_dir" ) );
		}
		return ImageProxy.instance;
	}

	HashMap<String, AbstractImage> images = new HashMap<>();
	

	public synchronized AbstractImage getImage( String name ) {

		logger.debug( "getting image " + name );

		if( ! images.containsKey( name ) ||
				( Boolean.parseBoolean( Tileserver.getProperty( "debug" ) ) &&
				Boolean.parseBoolean( Tileserver.getProperty( "debug_reload_image" ) ) ) ) {
			if( ! new File( Tileserver.getProperty("source_image_dir") + name + ".h5" ).exists() ) {
				throw new ImageNotFoundException( name );
			}
			logger.info( "loading image " + name );

			images.put( name, new HDF5YAMLImage( name ) );
		}
		//logger.trace( "current size of images map: " + GraphLayout.parseInstance( images ).totalSize() / 1024 + "kb" );
		return images.get( name );
	}
}
