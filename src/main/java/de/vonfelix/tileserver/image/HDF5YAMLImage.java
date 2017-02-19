package de.vonfelix.tileserver.image;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.vonfelix.tileserver.Adjustable.Adjustment;
import de.vonfelix.tileserver.Color;
import de.vonfelix.tileserver.exception.YamlConfigurationException;
import de.vonfelix.tileserver.stack.Channel;
import de.vonfelix.tileserver.stack.CompositeStack;
import de.vonfelix.tileserver.stack.HDF5Stack;
import de.vonfelix.tileserver.stack.IStack;

public class HDF5YAMLImage extends AbstractImage {

	private Environment env;

	static Logger logger = LogManager.getLogger();

	private	IHDF5Reader reader;
	

	// private ImageConfiguration imageConfiguration;
	
	private HashMap<String, Object> configuration = new HashMap<>();

	public HDF5YAMLImage( Environment env, String name ) {
		super( name );
		this.env = env;
	}

	@PostConstruct
	public void initialize() {
		
		logger.debug( "reading hdf5 for " + name );
		// open HDF5 file
		reader = HDF5Factory.openForReading( env.getProperty( "tilebuilder.source_image_dir" ) + name + ".h5" );
		

		// read image info from YAML
		try {
			logger.debug( "reading YAML for " + name );

			InputStream input = new FileInputStream( env.getProperty( "tilebuilder.source_image_dir" ) + name
					+ ".yaml" );
			Yaml yaml = new Yaml();
			configuration = (HashMap<String, Object>) yaml.load(input);

			HashMap project = (HashMap) ((HashMap<String, Object>) configuration).get("project");

			Integer min = project.containsKey("min") ? (Integer) project.get("min")
					: env.getProperty("tilebuilder.min", Integer.class);
			Integer max = project.containsKey("max") ? (Integer) project.get("max")
					: env.getProperty("tilebuilder.max", Integer.class);


			List yamlstacks = (List) project.get("stacks");



			/*
			 * READ STACKS
			 */
			for ( Object s : yamlstacks ) {
				HashMap<?, ?> st = (HashMap<?, ?>) s;

				/*
				 * Read Data from the list of "mirrors". If at least one entry
				 * is a mirror with tile source type 9, this is the stack
				 * description for TileBuilder. If no mirror with tile source
				 * type 9 is found, the image is not a valid TileBuilder image
				 * and an error is thrown.
				 */
				List mirrors = (List) st.get("mirrors");
				if (mirrors == null) {
					throw new YamlConfigurationException("Invalid YAML configuration for image '" + name
							+ "': YAML must define a valid 'mirrors' section for each stack.", configuration);
				}

				String id = null;
				String title = null;
				HashMap<String, String> m = null;
				for (Object mirror : mirrors) {
					m = (HashMap) mirror;
					String tileSourceType = (String) m.get("tile_source_type");
					if (tileSourceType.equals(env.getProperty("tilebuilder.tile_source_type", "9"))) {
						id = (String) m.get("folder");
						title = (String) m.get("title");
						break;
					}
				}
				if (m != null && (id == null || title == null)) {
					throw new YamlConfigurationException("Invalid YAML configuration for image '" + name
							+ "': Each stack must have exactly one mirror for tile source type 9 and contain 'folder' and 'title' keys. This is the stack in question: "
							+ mirrors, configuration);
				}
				m.put("tile_width", env.getProperty("tilebuilder.tile_size"));
				m.put("tile_height", env.getProperty("tilebuilder.tile_size"));

				/*
				 * If a Stack contains a key "channels", it's a composite stack.
				 */
				if ( st.containsKey( "channels" ) ) {
					min = st.containsKey("min") ? (Integer) st.get("min") : min;
					max = st.containsKey( "max" ) ? (Integer) st.get( "max" ) : max;

					logger.trace( "  composite stack " + id + " (" + title + ")" );

					CompositeStack cs = new CompositeStack( this, id, title );

					ArrayList ch = (ArrayList) st.get( "channels" );
					for ( Object c : ch ) {
						HashMap cha = (HashMap) c;
						String stack_id = (String) cha.get( "stack" );
						String color = (String) cha.get( "color" );
						min = cha.containsKey("min") ? (Integer) cha.get("min") : min;
						max = cha.containsKey( "max" ) ? (Integer) cha.get( "max" ) : max;

						logger.trace( "    " + stack_id + " : " + color );
						HDF5Stack stack = (HDF5Stack) stacks.get( stack_id );
						Channel channel = new Channel( stack, Color.valueOf( color.toUpperCase() ) );
						if (min != null) {
							channel.setAdjustment(Adjustment.MIN_VALUE, min);
						}
						if (max != null) {
							channel.setAdjustment( Adjustment.MAX_VALUE, max );
						}
						cs.addChannel( channel );
					}
					stacks.put( cs.getId(), cs );
				}
				/*
				 * Otherwise, if the Stack does not contain a key "channels",
				 * it's a normal stack.
				 */
				else {
					String path = "stacks/";
					max = st.containsKey( "max" ) ? (Integer) st.get( "max" ) : max;

					logger.trace( "  stack " + path + "" + id + " (" + title + ")" );

					IStack sta = new HDF5Stack( this, path, id, title );
					if (min != null) {
						sta.setMin(min);
					}
					if ( max != null ) {
						sta.setMax( max );
					}
					stacks.put( sta.getId(), sta );
				}
			}

			if ( stacks.size() == 0 ) {
				logger.warn( "No stacks found for image " + name );
			}

		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String getConfiguration() {

		// set options for YAML output
		final DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);

		Yaml y = new Yaml(options);
		String s = y.dump( configuration );
		logger.trace("Image configuration: " + configuration);

		return s;
	}

	// private void putConfigValues( AbstractStack stack, HashMap yamlStack ) {
	// for ( Map<K, V>.Entry<K, V> ) {
	// stack.setConfigurationValue( key, value );
	// }
	// }

	public IHDF5Reader getReader() {
		return reader;
	}

	
	@Override
	public String toString() {
		return name;
	}
}
