package de.vonfelix.tileserver.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings( "serial" )
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Image has faulty configuration")
public class YamlConfigurationException extends RuntimeException {
	static Logger logger = LogManager.getLogger();

	public YamlConfigurationException( String imageName ) {
		super();

		logger.error("Faulty YAML configuration for image \"" + imageName + "\".");
	}

	public YamlConfigurationException(String imageName, String reason) {
		super();

		logger.error("Faulty YAML configuration for image \"" + imageName + "\": " + reason);
	}

	public YamlConfigurationException(String message, Object data) {
		super();

		logger.error(message);
		logger.debug(data);
	}
}
