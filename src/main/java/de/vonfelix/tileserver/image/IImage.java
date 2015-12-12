package de.vonfelix.tileserver.image;

import de.vonfelix.tileserver.ValueLimit;
import de.vonfelix.tileserver.exception.StackNotFoundException;
import de.vonfelix.tileserver.stack.IStack;

public interface IImage extends ValueLimit {

	String getName();

	//	void setValueLimit( int valueLimit );
	//	int getValueLimit();
	
	int getNumStacks();

	IStack getStack( String name ) throws StackNotFoundException;
}
