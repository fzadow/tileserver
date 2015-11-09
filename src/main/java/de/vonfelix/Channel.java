package de.vonfelix;

public class Channel {
	
	HDF5Image hdf5Image;
	String name;
	long[] dimensions;
	
	public Channel( HDF5Image hdf5Image, String name ) {
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

	@Override
	public String toString() {
		return name + " (" + hdf5Image + ")";
	}

}
