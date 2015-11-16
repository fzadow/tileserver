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
	
	private	IHDF5Reader reader;
	private HashMap<String, IStack> stacks;
	
	int SIZE = 256;
	
	public HDF5Image( String name ){
		this.name= name;
		
		// open HDF5 file
		reader = HDF5Factory.openForReading( Tileserver.getProperty("source_image_dir") + name + ".h5" );
	}
	
	/** an {@link ArrayList} with all the {@link Stack}s in the image*/
	private HashMap<String, IStack> getStacks() {
		if( stacks == null ) {
			stacks = new HashMap<String, IStack>();
			
			try {
				// TODO sanity checks when loading channels (no dupes, only sane composites)
				System.out.println("HDF5Image: reading stack info for " + name );
				Document stackinfo = new SAXBuilder().build( Tileserver.getProperty("source_image_dir") + name + ".xml" );
				Element root = stackinfo.getRootElement();
				Namespace ns = root.getNamespace();
				
				for( Element s : root.getChild("Stacks", ns).getChildren() ) {
					System.out.println( "  stack " + s.getChildText( "path", ns ) + "" + s.getChildText( "name", ns ) + " (" + s.getChildText( "title", ns ) + ")" );
					Stack st =  new Stack( this, s.getChildText( "path", ns ), s.getChildText( "name", ns ) );
					stacks.put( st.getName(), st );
				}
				for( Element s : root.getChild("CompositeStacks", ns).getChildren() ) {
					System.out.println( "  composite stack " + s.getChildText( "name", ns ) + " (" + s.getChildText( "title", ns ) + ")" );
					CompositeStack cs = new CompositeStack( this, s.getChildText( "stack", ns ), s.getChildText( "title", ns ) );
					for( Element c : s.getChild( "Channels", ns ).getChildren() ) {
						System.out.println( "    " + c.getChildText( "stack", ns ) + " : " + c.getChildText( "color", ns ) );
						cs.addChannel( (Stack)getStack( c.getChildText( "stack", ns ) ), ChannelColor.valueOf( c.getChildText( "color", ns ).toUpperCase() ) );
					}
					stacks.put( cs.getName(), cs );
				}
				
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// look for stacks (groups in group "stacks/")
			//for( String stack_name : reader.object().getAllGroupMembers("stacks/") ) {
			//	if( reader.object().isGroup( "stacks/" + stack_name ) ) {
			//		stacks.add( new Stack( this, stack_name ) );
			//	}
			//}
		
			if( stacks.size() == 0 ) {
				System.out.println( "No stacks found!" );
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
