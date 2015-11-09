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
	public short[] getBlock( int size, int offset_z, int offset_x, int offset_y ) throws Exception {
		return hdf5Image.getReader().uint16().readMDArrayBlockWithOffset( getName(),
				new int[] {1, size, size},
				new long[] { offset_z, offset_x, offset_y } ).getAsFlatArray();		
	}
	

	@Override
	public String toString() {
		return name + " (" + hdf5Image + ")";
	}

}
