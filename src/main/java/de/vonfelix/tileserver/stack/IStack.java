package de.vonfelix.tileserver.stack;

import de.vonfelix.tileserver.image.IImage;

public interface IStack {

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

	public int getMin();

	public int getMax();

	public void setMin(int min);

	public void setMax(int max);
}
