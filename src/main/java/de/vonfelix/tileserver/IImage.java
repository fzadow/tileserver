package de.vonfelix.tileserver;

import de.vonfelix.tileserver.exception.StackNotFoundException;

public interface IImage extends ValueLimit {

	String getName();

	void setValueLimit( int valueLimit );
	int getValueLimit();
	
	int getNumStacks();

	IStack getStack( String name ) throws StackNotFoundException;
}
