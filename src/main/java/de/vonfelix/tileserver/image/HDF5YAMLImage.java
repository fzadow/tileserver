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

			// TODO set valueLimit

			// read stacks
			stacks = loadStacks( yamlstacks );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private HashMap<String, IStack> loadStacks( ArrayList yamlstacks ) {

		for ( Object s : yamlstacks ) {
			HashMap st = (HashMap) s;
			System.out.println( st.get( "name" ) );

			// Composite Stack
			if ( st.containsKey( "channels" ) ) {
				String id = (String) st.get( "folder" );
				String title = (String) st.get( "name" );

				logger.trace( "  composite stack " + id + " (" + title + ")" );

				CompositeStack cs = new CompositeStack( this, id, title );

				ArrayList ch = (ArrayList) st.get( "channels" );
				for ( Object c : ch ) {
					HashMap cha = (HashMap) c;
					String stack_id = (String) cha.get( "stack" );
					String color = (String) cha.get( "color" );

					logger.trace( "    " + stack_id + " : " + color );
					HDF5Stack stack = (HDF5Stack) stacks.get( stack_id );
					Channel channel = new Channel( stack, Color.valueOf( color.toUpperCase() ) );
					cs.addChannel( channel );
				}
				stacks.put( cs.getId(), cs );
			}
			// normal Stack
			else {
				String id = (String) st.get( "folder" );
				String title = (String) st.get( "name" );
				String path = "stacks/";

				logger.trace( "  stack " + path + "" + id + " (" + title + ")" );

				IStack sta = new HDF5Stack( this, path, id, title );
				stacks.put( sta.getId(), sta );
			}

		}

		//		if ( imageDesc != null ) {
		//			// TODO sanity checks when loading channels (no dupes, only sane
		//			// composites)
		//			logger.trace( "reading stack info for " + name );
		//			Element root = imageDesc.getRootElement();
		//			Namespace ns = root.getNamespace();
		//
		//			// load all normal Stacks (children of <Stacks> in xml)
		//			for ( Element s : root.getChild( "Stacks", ns ).getChildren() ) {
		//				logger.trace( "  stack " + s.getChildText( "path", ns ) + "" + s.getChildText( "id", ns ) + " (" + s.getChildText( "title", ns ) + "), value limit: " + s.getChildText( "value_limit", ns ) );
		//				// Stack st = new Stack( this, s.getChildText( "path", ns ),
		//				// s.getChildText( "id", ns ) );
		//
		//				String id = s.getChildText( "id", ns );
		//				String title = s.getChildText( "title", ns );
		//				String path = s.getChildText( "path", ns );
		//				IStack st = new HDF5Stack( this, path, id, title );
		//
		//				if ( s.getChildText( "value_limit", ns ) != null ) {
		//					st.setValueLimit( Integer.parseInt( s.getChildText( "value_limit", ns ) ) );
		//				}
		//				stacks.put( st.getId(), st );
		//			}
		//
		//			// load all composite Stacks (children of <CompositeStacks> in xml)
		//			for ( Element s : root.getChild( "CompositeStacks", ns ).getChildren() ) {
		//				logger.trace( "  composite stack " + s.getChildText( "id", ns ) + " (" + s.getChildText( "title", ns ) + ")" );
		//				CompositeStack cs = new CompositeStack( this, s.getChildText( "id", ns ), s.getChildText( "title", ns ) );
		//				for ( Element c : s.getChild( "Channels", ns ).getChildren() ) {
		//					logger.trace( "    " + c.getChildText( "stack_id", ns ) + " : " + c.getChildText( "color", ns ) + ", value limit: " + c.getChildText( "value_limit", ns ) );
		//					HDF5Stack stack = (HDF5Stack) stacks.get( c.getChildText( "stack_id", ns ) );
		//					Channel channel = new Channel( stack, Color.valueOf( c.getChildText( "color", ns ).toUpperCase() ) );
		//					if ( c.getChildText( "value_limit", ns ) != null ) {
		//						channel.setValueLimit( Integer.parseInt( c.getChildText( "value_limit", ns ) ) );
		//					}
		//					cs.addChannel( channel );
		//				}
		//				stacks.put( cs.getId(), cs );
		//			}
		//		} else {
		//			logger.warn( "No image description XML loaded!" );
		//		}

		if ( stacks.size() == 0 ) {
			logger.warn( "No stacks found for image " + name );
		}
		
		return stacks;
	}
	
	public IHDF5Reader getReader() {
		return reader;
	}

	
	@Override
	public String toString() {
		return name;
	}
}
