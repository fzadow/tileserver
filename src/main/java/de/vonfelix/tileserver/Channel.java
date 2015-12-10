package de.vonfelix.tileserver;

import org.apache.commons.lang.NotImplementedException;

public class Channel implements ValueLimit, Adjustable {

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
	public void setValueLimit( int valueLimit ) {
		this.valueLimit = valueLimit;
	}

	@Override
	public int getValueLimit() {
		if ( valueLimit == 0 ) {
			return simpleStack.getValueLimit();
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
