package de.vonfelix;

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
	private HashMap<String, IStack> stacks;
	private Document imageDesc;
	
	public HDF5Image( String name ){
		super( name );
		
		// open HDF5 file
		reader = HDF5Factory.openForReading( Tileserver.getProperty("source_image_dir") + name + ".h5" );
		
		// read image info from XML
		try {
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
		stacks = initStacks();
	}
	
	private HashMap<String, IStack> initStacks() {
		stacks = new HashMap<String, IStack>();

		if ( imageDesc != null ) {
			// TODO sanity checks when loading channels (no dupes, only sane
			// composites)
			Tileserver.log( "reading stack info for " + name );
			Element root = imageDesc.getRootElement();
			Namespace ns = root.getNamespace();

			for ( Element s : root.getChild( "Stacks", ns ).getChildren() ) {
				Tileserver.log( "  stack " + s.getChildText( "path", ns ) + "" + s.getChildText( "id", ns ) + " (" + s.getChildText( "title", ns ) + "), value limit: " + s.getChildText( "value_limit", ns ) );
				Stack st = new Stack( this, s.getChildText( "path", ns ), s.getChildText( "id", ns ) );
				if ( s.getChildText( "value_limit", ns ) != null ) {
					st.setValueLimit( Integer.parseInt( s.getChildText( "value_limit", ns ) ) );
				}
				stacks.put( st.getId(), st );
			}
			for ( Element s : root.getChild( "CompositeStacks", ns ).getChildren() ) {
				Tileserver.log( "  composite stack " + s.getChildText( "id", ns ) + " (" + s.getChildText( "title", ns ) + ")" );
				CompositeStack cs = new CompositeStack( this, s.getChildText( "id", ns ), s.getChildText( "title", ns ) );
				for ( Element c : s.getChild( "Channels", ns ).getChildren() ) {
					Tileserver.log( "    " + c.getChildText( "stack_id", ns ) + " : " + c.getChildText( "color", ns ) + ", value limit: " + c.getChildText( "value_limit", ns ) );
					Stack channel = new Stack( (Stack) stacks.get( c.getChildText( "stack_id", ns ) ) );
					if ( c.getChildText( "value_limit", ns ) != null ) {
						channel.setValueLimit( Integer.parseInt( c.getChildText( "value_limit", ns ) ) );
					}

					cs.addChannel( channel, ChannelColor.ColorName.valueOf( c.getChildText( "color", ns ).toUpperCase() ) );
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
	
	public int getNumStacks() {
		return stacks.size();
	}
	
	public IStack getStack( String name ) {
		return stacks.get( name );
	}
	
	public IHDF5Reader getReader() {
		return reader;
	}

	
	@Override
	public String toString() {
		return name;
	}
}
