package de.vonfelix.tileserver.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings( "serial" )
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Image has faulty configuration")
public class YamlConfigurationException extends RuntimeException {
	static Logger logger = LogManager.getLogger();

	private String message;
	private String messageDetail;

	public YamlConfigurationException( String imageName ) {
		super();
		this.message = String.format("Faulty YAML configuration for image \"%s\".", imageName);
	}

	public YamlConfigurationException(String imageName, String reason) {
		super();
		this.message = String.format("Faulty YAML configuration for image \"%s\": %s.", imageName, reason);
	}

	public YamlConfigurationException(String message, Object data) {
		super();

		this.message = message;
		this.messageDetail = data.toString();
	}

	public String getMessageDetail() {
		return messageDetail;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
