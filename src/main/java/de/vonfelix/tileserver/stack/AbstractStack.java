package de.vonfelix.tileserver.stack;

import java.util.HashMap;

import de.vonfelix.tileserver.image.AbstractImage;

public abstract class AbstractStack implements IStack {

	protected AbstractImage image;
	protected String id;
	protected String title;
	protected HashMap<Integer, long[]> dimensions;
	protected int max;
	protected int scaleLevels;

	public AbstractStack( AbstractImage image, String id, String title ) {
		this.image = image;
		this.id = id;
		this.title = title;
		this.dimensions = new HashMap<Integer, long[]>();
		this.max = image.getMax();
	}

	@Override
	public long getWidth( int scaleLevel ) {
		if ( dimensions.containsKey( scaleLevel ) ) {
			return dimensions.get( scaleLevel )[ 2 ];
		}
		return 0;
	}

	@Override
	public long getHeight( int scaleLevel ) {
		if ( dimensions.containsKey( scaleLevel ) ) {
			return dimensions.get( scaleLevel )[ 1 ];
		}
		return 0;
	}

	@Override
	public long getDepth( int scaleLevel ) {
		if ( dimensions.containsKey( scaleLevel ) ) {
			return dimensions.get( scaleLevel )[ 0 ];
		}
		return 0;
	}
	
	public AbstractImage getImage() {
		return image;
	}
	
	public String getId() {
		return id;
	}
	
	public void setTitle( String title ) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	
	public void setMax( int valueLimit ) {
		this.max = valueLimit;
	}

	public int getMax() {
		return max;
	}

	public int getScaleLevels() {
		return scaleLevels;
	}

	protected HashMap<Integer, long[]> getDimensions() {
		return dimensions;
	}

	public long[] getDimensions( int scale_level ) {
		return dimensions.get( scale_level );
	}

	@Override
	public String toString() {
		return id;
	}
}
