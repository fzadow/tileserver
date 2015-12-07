package de.vonfelix.tileserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus( value = HttpStatus.NOT_FOUND, reason = "Stack not found" )
public class StackNotFoundException extends RuntimeException {
	public StackNotFoundException( String stackName ) {
		super( "Stack " + stackName + " not found." );
		System.out.println( "stack not found" );
	}
}
