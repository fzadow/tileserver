package de.vonfelix;

public abstract class AbstractImage implements IImage {

	protected String name;
	protected int valueLimit = Integer.parseInt( Tileserver.getProperty( "tile_value_limit" ) );

	public AbstractImage( String name ) {
		this.name = name;
	}

	
	@Override
	public void setValueLimit( int valueLimit ) {
		this.valueLimit = valueLimit;
	}
	
	@Override
	public int getValueLimit() {
		return valueLimit;
	}
}
