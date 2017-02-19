package de.vonfelix.tileserver.stack;

import org.apache.commons.lang.NotImplementedException;

import de.vonfelix.tileserver.Adjustable;
import de.vonfelix.tileserver.Color;
import de.vonfelix.tileserver.MaxValue;

public class Channel implements MaxValue, Adjustable {

	private SimpleStack simpleStack;
	private Color color;
	private int min;
	private int max;

	public Channel( SimpleStack simpleStack, Color color ) {
		this.simpleStack= simpleStack;
		this.color = color;
	}

	public SimpleStack getStack() {
		return simpleStack;
	}

	public void setColor( Color color ) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	@Override
	public void setMax( int max ) {
		this.max = max;
	}

	@Override
	public int getMax() {
		if ( max == 0 ) {
			return simpleStack.getMax();
		}
		return max;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMin() {
		if (min == 0) {
			return simpleStack.getMin();
		}
		return min;
	}

	@Override
	public String toString() {
		return simpleStack.toString();
	}

	@Override
	public void setAdjustment( Adjustment adjustment, Object value ) {
		switch ( adjustment ) {
		case COLOR:
			this.color = (Color) value;
			break;
		case CONTRAST_EXPONENT:
			throw new NotImplementedException(
					"CONTRAST_EXPONENT adjustment not yet implemented in" + this.getClass() );
			// break;
		case MAX_VALUE:
			this.max = (int) value;
			break;
		case MIN_VALUE:
			this.min = (int) value;
			break;
		default:
			break;
		}

	}
}
