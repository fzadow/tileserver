package de.vonfelix;

public interface IImage {

	void setValueLimit( int valueLimit );
	int getValueLimit();
	
	int getNumStacks();
	IStack getStack( String name );
}
