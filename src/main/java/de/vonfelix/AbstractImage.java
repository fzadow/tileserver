package de.vonfelix;

import java.util.HashMap;

public abstract class AbstractImage implements IImage {

	protected String name;
	protected int valueLimit = Integer.parseInt( Tileserver.getProperty( "value_limit" ) );
	protected HashMap<String, IStack> stacks;
	

	public AbstractImage( String name ) {
		this.name = name;
		this.stacks = new HashMap<String, IStack>();
	}

	@Override
	public int getNumStacks() {
		return stacks.size();
	}

	@Override
	public IStack getStack( String name ) {
		return stacks.get( name );
	};

	@Override
	public String getName() {
		return name;
	};
	
	@Override
	public void setValueLimit( int valueLimit ) {
		this.valueLimit = valueLimit;
	}
	
	@Override
	public int getValueLimit() {
		return valueLimit;
	}
}
