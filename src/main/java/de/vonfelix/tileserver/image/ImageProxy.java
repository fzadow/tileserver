package de.vonfelix.tileserver.image;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

import de.vonfelix.tileserver.exception.ImageNotFoundException;

/**
 * This class handles loading and returning of {@link AbstractImage}s
 * 
 * @author felix
 *
 */
@Service
public class ImageProxy {

	@Autowired
	private Environment env;

	@Value( "${tilebuilder.debug.enabled}" )
	Boolean debug_enabled;

	@Value("${tilebuilder.debug.reload_image}")
	Boolean debug_reload_image;

	@Value( "${tilebuilder.source_image_dir}" )
	String source_image_dir;

	static Logger logger = LogManager.getLogger();

	HashMap<String, AbstractImage> images = new HashMap<>();

	public synchronized AbstractImage getImage( String name ) {

		logger.debug("Getting image " + name);

		if ( !images.containsKey( name ) || ( debug_enabled && debug_reload_image ) ) {
			if ( !new File( source_image_dir + name + ".h5" ).exists() ) {
				throw new ImageNotFoundException( name );
			}
			logger.info("Image " + name + " not yet loaded. Loading...");

			HDF5YAMLImage img = new HDF5YAMLImage( env, name );
			// img.initialize();
			images.put( name, img );
		}
		// logger.trace("current size of images map: " +
		// GraphLayout.parseInstance(images).totalSize() / 1024 + "kb");
		return images.get( name );
	}

	public String getList() {

		logger.debug( "getting list of all images" );
		long startTime = System.nanoTime();

		// FilenameFilter to get all YAML files
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

		File source_dir = new File( source_image_dir );
		ArrayList<File> files = new ArrayList<File>( Arrays.asList( source_dir.listFiles( yamlFilter ) ) );

		ArrayList<Object> data = new ArrayList<Object>();

		for ( File file : files ) {
			try {
				// Check if file has already been loaded
				String name = FilenameUtils.removeExtension(file.getName());
				HDF5YAMLImage image = (HDF5YAMLImage) images.get(name);

				if (image == null) {
					image = new HDF5YAMLImage(env, name);
				}

				String configurationYaml = image.getConfigurationYaml();
				if (configurationYaml != null) {
					data.add(configurationYaml);
				}
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
