package de.vonfelix.tileserver.stack;

import org.apache.commons.lang.NotImplementedException;

import de.vonfelix.tileserver.Adjustable;
import de.vonfelix.tileserver.Color;
import de.vonfelix.tileserver.MaxValue;

public class Channel implements MaxValue, Adjustable {

	private SimpleStack simpleStack;
	private Color color;
	private int valueLimit;

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
		this.valueLimit = max;
	}

	@Override
	public int getMax() {
		if ( valueLimit == 0 ) {
			return simpleStack.getMax();
		}
		return valueLimit;
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
			this.valueLimit = (int) value;
			break;
		case MIN_VALUE:
			throw new NotImplementedException( "MIN_VALUE adjustment not yet implemented in" + this.getClass() );
			// break;
		default:
			break;
		}

	}
}
