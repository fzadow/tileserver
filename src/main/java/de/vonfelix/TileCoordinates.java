package de.vonfelix;

public class TileCoordinates {
	
	private HDF5Image hdf5Image;

	private int size;

	private int slice_index;
	private int row_index;
	private int column_index;
	
	
	public TileCoordinates( HDF5Image hdf5Image, int size, int row_index, int column_index, int slice_index) {
		this.hdf5Image = hdf5Image;
		this.size = size;
		this.row_index = row_index;
		this.column_index = column_index;
		this.slice_index = slice_index;
	}
	
	public int getRowIndex() {
		return row_index;
	}
	
	public int getColumnIndex() {
		return column_index;
	}
	
	public int getSliceIndex() {
		return slice_index;
	}
	
	public int getX() {
		return size * row_index;
	}

	public int getY() {
		return size * column_index;
	}

	public int getZ() {
		return slice_index;
	}

	public int getSize() {
		return size;
	}
	
	@Override
	public String toString() {
		return "(" + slice_index + "," + row_index + "," + column_index + ") [" + getZ() + "," + getX() + "," + getY() + "]";

	}
	
}
