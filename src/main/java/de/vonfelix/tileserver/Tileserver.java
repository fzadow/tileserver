package de.vonfelix.tileserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Tileserver extends SpringBootServletInitializer {

	static Logger logger = LogManager.getLogger();

	private static int count = 0;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Tileserver.class);
	}

	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(Tileserver.class, args);
		logger.info("Running on port " + context.getEnvironment().getProperty("server.port"));
	}
	
	public static synchronized void countTile() {
		count++;
		if ( count % 1000 == 0 ) {
			logger.info( "Yay, just served my " + count + "th tile" );
		}
	}
}
