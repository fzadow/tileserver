package de.vonfelix.tileserver.image;

import java.util.HashMap;

import de.vonfelix.tileserver.Tileserver;
import de.vonfelix.tileserver.exception.StackNotFoundException;
import de.vonfelix.tileserver.stack.IStack;

public abstract class AbstractImage implements IImage {

	protected String name;
	protected int max = Integer.parseInt( Tileserver.getProperty( "max" ) );
	protected int min = Integer.parseInt( Tileserver.getProperty( "min" ) );
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
	public void setMax( int max ) {
		this.max = max;
	}
	
	@Override
	public int getMax() {
		return max;
	}
}
