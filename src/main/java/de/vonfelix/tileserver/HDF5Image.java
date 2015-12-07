package de.vonfelix.tileserver;

import java.io.IOException;
import java.util.HashMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

public class HDF5Image extends AbstractImage {

	private	IHDF5Reader reader;
	private Document imageDesc;
	
	public HDF5Image( String name ){
		super( name );
		
		Tileserver.log( "reading hdf5" );
		// open HDF5 file
		reader = HDF5Factory.openForReading( Tileserver.getProperty("source_image_dir") + name + ".h5" );
		
		// read image info from XML
		try {
			Tileserver.log( "reading xml" );

			imageDesc = new SAXBuilder().build( Tileserver.getProperty("source_image_dir") + name + ".xml" );
			Element root = imageDesc.getRootElement();
			Namespace ns = root.getNamespace();
			if ( root.getChildText( "value_limit", ns ) != null ) {
				valueLimit = Integer.parseInt( root.getChildText( "value_limit", ns ) );
			}
		} catch ( JDOMException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// read stacks from XML
		stacks = loadStacks();
	}
	
	private HashMap<String, IStack> loadStacks() {
		Tileserver.log( "reading stacks" );

		if ( imageDesc != null ) {
			// TODO sanity checks when loading channels (no dupes, only sane
			// composites)
			Tileserver.log( "reading stack info for " + name );
			Element root = imageDesc.getRootElement();
			Namespace ns = root.getNamespace();

			// load all normal Stacks (children of <Stacks> in xml)
			for ( Element s : root.getChild( "Stacks", ns ).getChildren() ) {
				Tileserver.log( "  stack " + s.getChildText( "path", ns ) + "" + s.getChildText( "id", ns ) + " (" + s.getChildText( "title", ns ) + "), value limit: " + s.getChildText( "value_limit", ns ) );
				// Stack st = new Stack( this, s.getChildText( "path", ns ),
				// s.getChildText( "id", ns ) );

				String id = s.getChildText( "id", ns );
				String title = s.getChildText( "title", ns );
				String path = s.getChildText( "path", ns );
				IStack st = new HDF5Stack( this, path, id, title );

				if ( s.getChildText( "value_limit", ns ) != null ) {
					st.setValueLimit( Integer.parseInt( s.getChildText( "value_limit", ns ) ) );
				}
				stacks.put( st.getId(), st );
			}

			// load all composite Stacks (children of <CompositeStacks> in xml)
			for ( Element s : root.getChild( "CompositeStacks", ns ).getChildren() ) {
				Tileserver.log( "  composite stack " + s.getChildText( "id", ns ) + " (" + s.getChildText( "title", ns ) + ")" );
				CompositeStack cs = new CompositeStack( this, s.getChildText( "id", ns ), s.getChildText( "title", ns ) );
				for ( Element c : s.getChild( "Channels", ns ).getChildren() ) {
					Tileserver.log( "    " + c.getChildText( "stack_id", ns ) + " : " + c.getChildText( "color", ns ) + ", value limit: " + c.getChildText( "value_limit", ns ) );
					HDF5Stack stack = (HDF5Stack) stacks.get( c.getChildText( "stack_id", ns ) );
					Channel channel = new Channel( stack, Color.valueOf( c.getChildText( "color", ns ).toUpperCase() ) );
					if ( c.getChildText( "value_limit", ns ) != null ) {
						channel.setValueLimit( Integer.parseInt( c.getChildText( "value_limit", ns ) ) );
					}
					cs.addChannel( channel );
				}
				stacks.put( cs.getId(), cs );
			}
		} else {
			Tileserver.log( "No image description XML loaded!" );
		}

		if ( stacks.size() == 0 ) {
			Tileserver.log( "No stacks found!" );
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