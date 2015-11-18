package de.vonfelix;

/**
 * Describes the location and dimensions of a tile inside a Stack
 * 
 * @author felix
 *
 */

public class TileCoordinates {
	
	private int size;

	private int scale_level;
	private int slice_index;
	private int row_index;
	private int column_index;
	
	
	public TileCoordinates( int size, int scale_level, int row_index, int column_index, int slice_index) {
		this.size = size;
		this.scale_level = scale_level;
		this.row_index = row_index;
		this.column_index = column_index;
		this.slice_index = slice_index;
	}
	
	public int getScaleLevel() {
		return scale_level;
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
		return "Coordinates: (" + slice_index + "," + row_index + "," + column_index + ") [" + getZ() + "," + getX() + "," + getY() + "]";

	}
	
}
