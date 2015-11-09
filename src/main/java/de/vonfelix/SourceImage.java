package de.vonfelix;

import java.util.ArrayList;

/** a representation of a multi-channel image **/
public abstract class SourceImage implements IImage {

	/** list of stacks **/
	protected ArrayList<Stack> stacks;
	
}
