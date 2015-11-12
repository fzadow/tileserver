package de.vonfelix;

import java.util.ArrayList;

import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

public class HDF5Image {


	private String filename;
	
	private	IHDF5Reader reader;
	private ArrayList<Stack> stacks;
	
	int SIZE = 256;
	
	public HDF5Image( String filename ){
		this.filename= filename;
		
		// open HDF5 file
		reader = HDF5Factory.openForReading( filename );
	}
	
	/** an {@link ArrayList} with all the {@link Stack}s in the image*/
	private ArrayList<Stack> getStacks() {
		if( stacks == null ) {
			stacks = new ArrayList<Stack>();
			
			try {

			} catch ( Exception e ) {
				e.printStackTrace();
			}
			
			// look for stacks (groups in group "stacks/")
			for( String stack_name : reader.object().getAllGroupMembers("stacks/") ) {
				if( reader.object().isGroup( "stacks/" + stack_name ) ) {
					stacks.add( new Stack( this, stack_name ) );
				}
			}
		
			if( stacks.size() == 0 ) {
				System.out.println( "No stacks found!" );
			}
		}
		
		return stacks;
	}
	
	public int getNumChannels() {
		return getStacks().size();
	}
	
	public Stack getStack( int n ) {
		return getStacks().get( n );
	}
	
	public Stack getStack( String name ) {
		for( Stack stack : getStacks() ) {
			if( stack.getName().equals( name ) ) {
				return stack;
			}
		}
		return null;
	}
	
	public IHDF5Reader getReader() {
		return reader;
	}

	
	@Override
	public String toString() {
		return filename;
	}
}
