package de.vonfelix;

import ch.systemsx.cisd.base.mdarray.MDShortArray;

public class Stack {
	
	HDF5Image hdf5Image;
	String name;
	long[] dimensions;
	
	public Stack( HDF5Image hdf5Image, String name ) {
		this.hdf5Image= hdf5Image;
		this.name= name;
	}
	
	public String getName() {
		return name;
	}
	
	public long[] getDimensions() {
		dimensions = dimensions != null ? dimensions : hdf5Image.getReader().object().getDimensions( getName() );
		return dimensions;
	}


	/** 
	 * return a flat array for the requested block
	 * @param offset_z slice
	 * @return a flattened array of the block */
	public MDShortArray getBlock( int size, int offset_z, long offset_x, long offset_y ) throws Exception {
		System.out.println( "image bounds: " + getDimensions()[1] + "x" + getDimensions()[2] );
		
		// restrict block loading to image bounds
		int width = offset_x + size > getDimensions()[1] ? (int) ( getDimensions()[1] - offset_x - 1) : size;
		int height = offset_y + size > getDimensions()[2] ? (int) ( getDimensions()[2] - offset_y - 1 ) : size;
		
		System.out.println( "getting " + width + "x" + height + " block from " + offset_x + "," + offset_y + " to "  + ( width + offset_x ) + "," + ( height + offset_y ) );
		
		
		return hdf5Image.getReader().uint16().readMDArrayBlockWithOffset( getName(),
				new int[] {1, width, height},
				new long[] { offset_z, offset_x, offset_y } );
	}
	

	@Override
	public String toString() {
		return name + " (" + hdf5Image + ")";
	}

}
