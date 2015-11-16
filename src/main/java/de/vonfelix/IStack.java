package de.vonfelix;

public interface IStack {

	public HDF5Image getHdf5Image();
	public String getName();
	public String getTitle();
	
	/**
	 * 
	 * @param scaleLevel
	 * @return
	 */
	public long[] getDimensions( int scaleLevel);
	
	/**
	 * @return number of scale levels
	 */
	public int getNumScaleLevels();

}
