package de.vonfelix.tileserver.image;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import de.vonfelix.tileserver.exception.StackNotFoundException;
import de.vonfelix.tileserver.stack.IStack;

public abstract class AbstractImage implements IImage {

	@Autowired
	private Environment env;

	protected String name;

	@Value( "${tilebuilder.min}" )
	protected int min;

	@Value( "${tilebuilder.max}" )
	protected int max;

	protected HashMap<String, IStack> stacks = new HashMap<>();
	// protected HashMap<String, V> stackgroups = new HashMap<>();
	
	protected HashMap<String, Object> configurationValues = new HashMap<>();

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
	public void setMin(int min) {
		this.min = min;
	}

	@Override
	public int getMin() {
		return min;
	}
	
	@Override
	public void setMax( int max ) {
		this.max = max;
	}
	
	@Override
	public int getMax() {
		return max;
	}

	public Object getConfiguration() {
		return null;
	}
}
