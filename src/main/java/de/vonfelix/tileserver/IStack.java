package de.vonfelix.tileserver;

public interface IStack extends ValueLimit {

	public IImage getImage();
	public String getId();
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
	public int getScaleLevels();

	public long getWidth( int scaleLevel );

	public long getHeight( int scaleLevel );

	public long getDepth( int scaleLevel );
}
