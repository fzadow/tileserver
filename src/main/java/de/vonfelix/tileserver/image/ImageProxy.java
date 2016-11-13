package de.vonfelix.tileserver.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

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

	public String getList() {

		logger.debug( "getting list of all images" );
		long startTime = System.nanoTime();

		FilenameFilter yamlFilter = new FilenameFilter() {
			@Override
			public boolean accept( File dir, String name ) {
				if ( name.lastIndexOf( '.' ) > 0 ) {
					// get last index for '.' char
					int lastIndex = name.lastIndexOf( '.' );

					// get extension
					String str = name.substring( lastIndex );

					// match path name extension
					if ( str.equals( ".yaml" ) ) {
						return true;
					}
				}
				return false;
			}
		};

		File source_dir = new File( Tileserver.getProperty( "source_image_dir" ) );
		ArrayList<File> files = new ArrayList<File>( Arrays.asList( source_dir.listFiles( yamlFilter ) ) );

		ArrayList<Object> data = new ArrayList<Object>();

		for ( File file : files ) {
			try {
				Yaml yaml = new Yaml();
				data.add( yaml.load( new FileInputStream( file ) ) );

				// list.put( file.getName(), new String( Files.readAllBytes(
				// Paths.get( file.getPath() ) ) ) );
			} catch ( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch ( ScannerException e ) {
				logger.error( "Error loading YAML file: " + file.getName() );
			}
		}

		DumperOptions options = new DumperOptions();
		options.setExplicitStart( true );
		options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
		Yaml yaml = new Yaml( options );

		long duration = ( System.nanoTime() - startTime );
		logger.debug( "listing all images took " + ( duration / 1000000 ) + " ms" );

		return yaml.dumpAll( data.iterator() );
	}
}
