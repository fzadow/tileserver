package de.vonfelix;

import java.util.HashMap;

public abstract class AbstractStack implements IStack {

	protected HDF5Image hdf5Image;
	protected String name;
	protected String title;
	protected HashMap<Integer,long[]> dimensions;

	public AbstractStack( HDF5Image hdf5Image, String name, String title ) {
		this.hdf5Image= hdf5Image;
		this.name= name;
		this.title= title;
		this.dimensions = new HashMap<Integer,long[]>();

	}
	
	public HDF5Image getHdf5Image() {
		return hdf5Image;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return name + " (" + hdf5Image + ")";
	}
}
