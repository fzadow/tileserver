package de.vonfelix.tileserver;

/**
 * Describes the location and dimensions of a tile inside a Stack
 * 
 * @author felix
 *
 */

public class TileCoordinates {
	
	private final int width;
	private final int height;

	private final int scale_level;
	private final int column_index, row_index, slice_index;
	private final int x, y, z;
	
	/**
	 * create tile coordinates with square dimensions and position specified as
	 * column_index, row_index and slice_index
	 * 
	 * @param size
	 * @param scale_level
	 * @param column_index
	 * @param row_index
	 * @param slice_index
	 */
	public TileCoordinates( int size, int scale_level, int column_index, int row_index, int slice_index ) {
		this.width = this.height = size;
		this.scale_level = scale_level;
		this.column_index = column_index;
		this.row_index = row_index;
		this.slice_index = this.z = slice_index;
		this.x = column_index * size;
		this.y = row_index * size;
	}
	
	public TileCoordinates( int width, int height, int scale_level, int x, int y, int z ) {
		this.width = width;
		this.height = height;
		this.scale_level = scale_level;
		this.x = x;
		this.y = y;
		this.z = this.slice_index = z;
		this.column_index = x / width;
		this.row_index = y / height;
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
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	@Override
	public String toString() {
		return "[x" + column_index + "/" + getX() + ", y" + row_index + "/" + getY() + ", z" + slice_index + ", " + ( 100 / Math.floor( Math.pow( 2, scale_level ) ) ) + "%]";

	}
	
}
