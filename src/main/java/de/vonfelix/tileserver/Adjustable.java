package de.vonfelix.tileserver;

public interface Adjustable {

	public static enum Adjustment {
		COLOR,
		MAX_VALUE,
		MIN_VALUE,
		CONTRAST_EXPONENT
	}

	public void setAdjustment( Adjustment adjustment, Object value );
}
