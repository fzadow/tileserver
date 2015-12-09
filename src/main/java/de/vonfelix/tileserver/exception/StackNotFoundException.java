package de.vonfelix.tileserver.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings( "serial" )
@ResponseStatus( value = HttpStatus.NOT_FOUND, reason = "Stack not found" )
public class StackNotFoundException extends RuntimeException {
	static Logger logger = LogManager.getLogger( StackNotFoundException.class.getName() );

	public StackNotFoundException( String stackName ) {
		super();

		logger.warn( "Stack " + stackName + " not found" );
	}
}
