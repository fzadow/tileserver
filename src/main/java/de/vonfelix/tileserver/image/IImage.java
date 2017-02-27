package de.vonfelix.tileserver.image;

import de.vonfelix.tileserver.exception.StackNotFoundException;
import de.vonfelix.tileserver.stack.IStack;

public interface IImage {

	String getName();

	//	void setValueLimit( int valueLimit );
	//	int getValueLimit();
	
	int getNumStacks();

	IStack getStack( String name ) throws StackNotFoundException;

	public void setMin(int min);

	public int getMin();

	public void setMax(int max);

	public int getMax();

	public String getConfigurationYaml();
}
