package de.vonfelix.tileserver.image;

import de.vonfelix.tileserver.MaxValue;
import de.vonfelix.tileserver.exception.StackNotFoundException;
import de.vonfelix.tileserver.stack.IStack;

public interface IImage extends MaxValue {

	String getName();

	//	void setValueLimit( int valueLimit );
	//	int getValueLimit();
	
	int getNumStacks();

	IStack getStack( String name ) throws StackNotFoundException;
}
