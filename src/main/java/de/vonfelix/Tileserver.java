package de.vonfelix;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Tileserver extends SpringBootServletInitializer {

	private static Properties properties;
	
	static String getProperty( String key ) {
		if( properties == null || Boolean.parseBoolean( properties.getProperty( "debug" ) ) == true )
			initProperties();
		
		return properties.getProperty( key );
	}
	
	static boolean hasProperty( String key ) {
		if( properties == null || Boolean.parseBoolean( properties.getProperty( "debug" ) ) == true  )
			initProperties();
		
		return properties.containsKey( key );
	}
	
	public static void log( String message ) {
		System.out.println( message + Thread.currentThread().getId() );
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Tileserver.class);
	}

	public static void main(String[] args) {

		// launch Spring application

		ApplicationContext ctx = SpringApplication.run(Tileserver.class, args);

        // list provided beans
        
        /*
        System.out.println( "Beans provided by Spring boot: " );
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort( beanNames );
        
        for( String beanName : beanNames ) {
        	System.out.println( beanName );
        }
        //*/
    }
	
	private static void initProperties() {
		properties = new Properties();
		InputStream input = null;

		try {
			// load properties from configuration file in project
			input = new FileInputStream( "config.properties" );
			properties.load( input );
			
			// additionally load properties from system global configuration
			// file, overwriting any properties with the same key.
			if( new File("/opt/etc/tileserver/config.properties").exists() ) {
				Properties system_properties = new Properties();
				system_properties.load( new FileInputStream( "/opt/etc/tileserver/config.properties" ) );
				properties.putAll( system_properties );
			}
			
		} catch ( IOException io ) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
