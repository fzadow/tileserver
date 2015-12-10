package de.vonfelix.tileserver.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.vonfelix.tileserver.IStack;
import de.vonfelix.tileserver.TileCoordinates;

@SuppressWarnings( "serial" )
@ResponseStatus( value = HttpStatus.NOT_FOUND, reason = "Tile out of bounds" )
public class TileOutOfBoundsException extends RuntimeException {
	static Logger logger = LogManager.getLogger();

	public TileOutOfBoundsException( IStack stack, TileCoordinates coordinates ) {
		super();

		logger.error( "requested tile out of bounds (stack size: " + stack.getWidth( coordinates.getScaleLevel() ) + "x" + stack.getHeight( coordinates.getScaleLevel() ) + "x" + stack.getDepth( coordinates.getScaleLevel() ) + ", requested tile starts at: " + coordinates.getX() + "," + coordinates.getY() + "," + coordinates.getZ() + ")" );
	}
}
