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

	private Boolean loaded = false;
	private Boolean loading = false;

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

	public abstract void loadImage();

	@Override
	public int getNumStacks() {
		return stacks.size();
	}

	@Override
	public IStack getStack( String name ) throws StackNotFoundException {
		if (!loaded && !loading) {
			synchronized (this) {
				if (!loaded && !loading) {
					loading = true;
					loadImage();
					loading = false;
				}
			}
		}
		if ( stacks.get( name ) == null ) {
			throw new StackNotFoundException( name );
		}
		return stacks.get( name );
	}

	@Override
	public String getName() {
		if (!loaded && !loading) {
			synchronized (this) {
				if (!loaded && !loading) {
					loading = true;
					loadImage();
					loading = false;
				}
			}
		}
		return name;
	};

	@Override
	public void setMin(int min) {
		this.min = min;
	}

	@Override
	public int getMin() {
		if (!loaded && !loading) {
			synchronized (this) {
				if (!loaded && !loading) {
					loading = true;
					loadImage();
					loading = false;
				}
			}
		}
		return min;
	}
	
	@Override
	public void setMax( int max ) {
		this.max = max;
	}
	
	@Override
	public int getMax() {
		if (!loaded && !loading) {
			synchronized (this) {
				if (!loaded && !loading) {
					loading = true;
					loadImage();
					loading = false;
				}
			}
		}
		return max;
	}

}
