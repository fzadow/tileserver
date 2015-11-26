package de.vonfelix;

public interface IImage extends ValueLimit {

	String getName();

	void setValueLimit( int valueLimit );
	int getValueLimit();
	
	int getNumStacks();
	IStack getStack( String name );
}
