package de.vonfelix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

public class HDF5Image {


	private String name;
	private int valueLimit = Integer.parseInt( Tileserver.getProperty( "tile_value_limit" ) );
	
	private	IHDF5Reader reader;
	private HashMap<String, IStack> stacks;
	private Document imageDesc;
	
	public HDF5Image( String name ){
		this.name= name;
		
		// open HDF5 file
		reader = HDF5Factory.openForReading( Tileserver.getProperty("source_image_dir") + name + ".h5" );
		
		// read image info from XML
		try {
			imageDesc = new SAXBuilder().build( Tileserver.getProperty("source_image_dir") + name + ".xml" );
			Element root = imageDesc.getRootElement();
			Namespace ns = root.getNamespace();
			if( root.getChildText( "tile_value_limit", ns ) != null ) {
				this.valueLimit = Integer.parseInt( root.getChildText( "tile_value_limit", ns ) );
			}
		} catch ( JDOMException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void setValueLimit( int valueLimit ) {
		this.valueLimit = valueLimit;
	}
	public int getValueLimit() {
		return valueLimit;
	}

	
	/** an {@link ArrayList} with all the {@link Stack}s in the image*/
	private HashMap<String, IStack> getStacks() {
		if( stacks == null ) {
			stacks = new HashMap<String, IStack>();
			
			if( imageDesc != null ) {
				// TODO sanity checks when loading channels (no dupes, only sane composites)
				System.out.println("HDF5Image: reading stack info for " + name );
				Element root = imageDesc.getRootElement();
				Namespace ns = root.getNamespace();
				
				for( Element s : root.getChild("Stacks", ns).getChildren() ) {
					System.out.println( "  stack " + s.getChildText( "path", ns ) + "" + s.getChildText( "name", ns ) + " (" + s.getChildText( "title", ns ) + "), value limit: " + s.getChildText( "tile_value_limit", ns ) );
					Stack st =  new Stack( this, s.getChildText( "path", ns ), s.getChildText( "name", ns ) );
					if( s.getChildText( "tile_value_limit", ns ) != null ) {
						st.setValueLimit( Integer.parseInt( s.getChildText( "tile_value_limit", ns ) ) );
					}
					stacks.put( st.getName(), st );
				}
				for( Element s : root.getChild("CompositeStacks", ns).getChildren() ) {
					System.out.println( "  composite stack " + s.getChildText( "name", ns ) + " (" + s.getChildText( "title", ns ) + ")" );
					CompositeStack cs = new CompositeStack( this, s.getChildText( "name", ns ), s.getChildText( "title", ns ) );
					for( Element c : s.getChild( "Channels", ns ).getChildren() ) {
						System.out.println( "    " + c.getChildText( "stack", ns ) + " : " + c.getChildText( "color", ns ) + ", value limit: " + c.getChildText( "tile_value_limit", ns ) );
						Stack channel = (Stack)stacks.get( c.getChildText( "stack", ns  ) );
						if( c.getChildText( "tile_value_limit", ns ) != null ) {
							channel.setValueLimit( Integer.parseInt( c.getChildText( "tile_value_limit", ns ) ) );
						}

						cs.addChannel( channel, ChannelColor.ColorName.valueOf( c.getChildText( "color", ns ).toUpperCase() ) );
					}
					stacks.put( cs.getName(), cs );
				}
			} else {
				System.out.println( "HDF5Image: No image description XML loaded!" );
			}

			if( stacks.size() == 0 ) {
				System.out.println( "HDF5Image: No stacks found!" );
			}
		}
		
		return stacks;
	}
	
	public int getNumChannels() {
		return getStacks().size();
	}
	
	public IStack getStack( String name ) {
		return getStacks().get( name );
	}
	
	public IHDF5Reader getReader() {
		return reader;
	}

	
	@Override
	public String toString() {
		return name;
	}
}
