package de.vonfelix.tileserver;

import java.util.HashMap;

import de.vonfelix.tileserver.exception.StackNotFoundException;

public abstract class AbstractImage implements IImage {

	protected String name;
	protected int valueLimit = Integer.parseInt( Tileserver.getProperty( "value_limit" ) );
	protected HashMap<String, IStack> stacks = new HashMap<>();
	
	public AbstractImage( String name ) {
		this.name = name;
	}

	@Override
	public int getNumStacks() {
		return stacks.size();
	}

	@Override
	public IStack getStack( String name ) throws StackNotFoundException {
		if ( stacks.get( name ) == null ) {
			throw new StackNotFoundException( name );
		}
		return stacks.get( name );
	}

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
