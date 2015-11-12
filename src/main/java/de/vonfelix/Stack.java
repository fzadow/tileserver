package de.vonfelix;

import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5DataClass;

public class Stack {
	
	HDF5Image hdf5Image;
	String name;
	List<String> scaleLevels;
	HashMap<Integer,long[]> dimensions;
	
	/**
	 * 
	 * @param hdf5Image reference to the HDF5Image containing the Image
	 * @param name Name to identify the Stack within the HDF5 file
	 */
	public Stack( HDF5Image hdf5Image, String name ) {
		this.hdf5Image= hdf5Image;
		this.name = name;
		this.dimensions = new HashMap<Integer,long[]>();

		scaleLevels = hdf5Image.getReader().object().getAllGroupMembers(
				Tileserver.getProperty("hdf5_stack_path") + name + "/" );
		
		// TODO get stack info (max value)
	}
	
	public Object readProperty( String name ) {
		//hdf5Image.getReader().object().getAllAttributeNames(arg0)
		return null;
	}
	
	public int getNumScaleLevels() {
		return scaleLevels.size();
	}
	
	public HDF5Image getHdf5Image() {
		return hdf5Image;
	}
	
	public String getName() {
		return name;
	}
	
	public String getFullName() {
		return Tileserver.getProperty("hdf5_stack_path") + this.getName();
	}
	
	public long[] getDimensions( int scaleLevel) {

		if( ! dimensions.containsKey( scaleLevel ) ) {
			dimensions.put(scaleLevel, hdf5Image.getReader().object().getDimensions( 
					Tileserver.getProperty("hdf5_stack_path") + getName() + "/" + scaleLevel ) );
		}
		return dimensions.get( scaleLevel );
	}


	/** 
	 * return a flat array for the requested block
	 * @return a flattened array of the block */
	public MDShortArray getBlock( int scaleLevel, int size, int offset_z, long offset_x, long offset_y ) throws Exception {
		System.out.println( "Stack: image bounds: " + getDimensions( scaleLevel )[1] + "x" + getDimensions( scaleLevel )[2] );
		
		// restrict block loading to image bounds
		int width = offset_x + size > getDimensions( scaleLevel )[1] ? (int) ( getDimensions( scaleLevel )[1] - offset_x - 1) : size;
		int height = offset_y + size > getDimensions( scaleLevel )[2] ? (int) ( getDimensions( scaleLevel )[2] - offset_y - 1 ) : size;
		
		System.out.println( "Stack: getting " + width + "x" + height + " block from " + offset_x + "," + offset_y + " to "  + ( width + offset_x ) + "," + ( height + offset_y ) );
		
		
		return hdf5Image.getReader().uint16().readMDArrayBlockWithOffset( getFullName() + "/" + scaleLevel,
				new int[] {1, width, height},
				new long[] { offset_z, offset_x, offset_y } );
	}
	

	@Override
	public String toString() {
		return name + " (" + hdf5Image + ")";
	}

}
