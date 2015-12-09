package de.vonfelix.tileserver.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings( "serial" )
@ResponseStatus( value = HttpStatus.NOT_FOUND, reason = "Image not found" )
public class ImageNotFoundException extends RuntimeException {
	static Logger logger = LogManager.getLogger();

	public ImageNotFoundException( String imageName ) {
		super();

		logger.error( "Image " + imageName + " not found" );
	}
}
