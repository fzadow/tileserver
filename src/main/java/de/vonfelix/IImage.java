package de.vonfelix;

public interface IImage {

	String getName();

	void setValueLimit( int valueLimit );
	int getValueLimit();
	
	int getNumStacks();
	IStack getStack( String name );
}
