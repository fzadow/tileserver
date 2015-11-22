package de.vonfelix;

import java.util.HashMap;

public abstract class AbstractStack implements IStack {

	protected HDF5Image hdf5Image;
	protected String id;
	protected String title;
	protected HashMap<Integer,long[]> dimensions;
	protected int valueLimit;

	
	public AbstractStack( HDF5Image hdf5Image, String id, String title ) {
		this.hdf5Image= hdf5Image;
		this.id= id;
		this.title= title;
		this.dimensions = new HashMap<Integer,long[]>();
		this.valueLimit = hdf5Image.getValueLimit();
	}
	
	public HDF5Image getHdf5Image() {
		return hdf5Image;
	}
	
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setValueLimit( int valueLimit ) {
		this.valueLimit = valueLimit;
	}
	public int getValueLimit() {
		return valueLimit;
	}
	
	@Override
	public String toString() {
		return id + " (" + hdf5Image + ")";
	}
}
