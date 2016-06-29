package de.vonfelix.tileserver.image;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import de.vonfelix.tileserver.Adjustable.Adjustment;
import de.vonfelix.tileserver.Color;
import de.vonfelix.tileserver.Tileserver;
import de.vonfelix.tileserver.stack.Channel;
import de.vonfelix.tileserver.stack.CompositeStack;
import de.vonfelix.tileserver.stack.HDF5Stack;
import de.vonfelix.tileserver.stack.IStack;

public class HDF5YAMLImage extends AbstractImage {

	static Logger logger = LogManager.getLogger();

	private	IHDF5Reader reader;
	
	private class YAMLInfo {
		public String name;
	}

	public HDF5YAMLImage( String name ) {
		super( name );
		
		logger.debug( "reading hdf5 for " + name );
		// open HDF5 file
		reader = HDF5Factory.openForReading( Tileserver.getProperty("source_image_dir") + name + ".h5" );
		
		// read image info from YAML
		try {
			logger.debug( "reading YAML for " + name );

			InputStream input = new FileInputStream( Tileserver.getProperty( "source_image_dir" ) + name + ".yaml" );
			Yaml yaml = new Yaml();
			HashMap data = (HashMap) yaml.load( input );

			ArrayList yamlstacks = (ArrayList) ( (HashMap) ( (HashMap) data ).get( "project" ) ).get( "stacks" );

			Integer max = (Integer) ( (HashMap) ( (HashMap) data ).get( "project" ) ).get( "max" );

			// TODO set valueLimit


			// READ STACKS

			for ( Object s : yamlstacks ) {
				HashMap st = (HashMap) s;

				// Composite Stack
				if ( st.containsKey( "channels" ) ) {
					String id = (String) st.get( "folder" );
					String title = (String) st.get( "name" );
					max = st.containsKey( "max" ) ? (Integer) st.get( "max" ) : max;

					logger.trace( "  composite stack " + id + " (" + title + ")" );

					CompositeStack cs = new CompositeStack( this, id, title );

					ArrayList ch = (ArrayList) st.get( "channels" );
					for ( Object c : ch ) {
						HashMap cha = (HashMap) c;
						String stack_id = (String) cha.get( "stack" );
						String color = (String) cha.get( "color" );
						max = cha.containsKey( "max" ) ? (Integer) cha.get( "max" ) : max;

						logger.trace( "    " + stack_id + " : " + color );
						HDF5Stack stack = (HDF5Stack) stacks.get( stack_id );
						Channel channel = new Channel( stack, Color.valueOf( color.toUpperCase() ) );
						if ( max != null )
							channel.setAdjustment( Adjustment.MAX_VALUE, max );
						cs.addChannel( channel );
					}
					stacks.put( cs.getId(), cs );
				}
				// normal Stack
				else {
					String id = (String) st.get( "folder" );
					String title = (String) st.get( "name" );
					String path = "stacks/";
					max = st.containsKey( "max" ) ? (Integer) st.get( "max" ) : max;

					logger.trace( "  stack " + path + "" + id + " (" + title + ")" );

					IStack sta = new HDF5Stack( this, path, id, title );
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
	

	public IHDF5Reader getReader() {
		return reader;
	}

	
	@Override
	public String toString() {
		return name;
	}
}
