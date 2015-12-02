package de.vonfelix;

import java.util.HashMap;

public abstract class AbstractStack implements IStack {

	protected AbstractImage image;
	protected String id;
	protected String title;
	protected HashMap<Integer,long[]> dimensions;
	protected int valueLimit;
	protected int scaleLevels;

	
	public AbstractStack( AbstractImage image, String id, String title ) {
		this.image = image;
		this.id= id;
		this.title = title;
		this.dimensions = new HashMap<Integer,long[]>();
		this.valueLimit = image.getValueLimit();
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
	
	public void setValueLimit( int valueLimit ) {
		this.valueLimit = valueLimit;
	}
	public int getValueLimit() {
		return valueLimit;
	}

	public int getScaleLevels() {
		return scaleLevels;
	}

	public HashMap<Integer, long[]> getDimensions() {
		return dimensions;
	}

	public long[] getDimensions( int scale_level ) {
		return dimensions.get( scale_level );
	}

	@Override
	public String toString() {
		return id + " (" + image + ")";
	}
}
