package de.vonfelix.tileserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

// TODO when several simultaneous requests for 1 image that has not been loaded before are made, some may not yet have the HDF5Image object.
//

@SpringBootApplication
public class Tileserver extends SpringBootServletInitializer {

	private static Properties properties;
	private static HashMap<Long, ArrayList<String>> log = new HashMap<Long, ArrayList<String>>();
	private static HashMap<Long, Long> startTimes = new HashMap<Long, Long>();
	
	static String getProperty( String key ) {
		if ( properties == null || Boolean.parseBoolean( properties.getProperty( "debug" ) ) == true )
			initProperties();
		
		return properties.getProperty( key );
	}
	
	static boolean hasProperty( String key ) {
		if ( properties == null || Boolean.parseBoolean( properties.getProperty( "debug" ) ) == true )
			initProperties();
		
		return properties.containsKey( key );
	}
	

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Tileserver.class);
	}

	public static void main(String[] args) {

		// launch Spring application

		initProperties();

		System.out.println();

		ApplicationContext ctx = SpringApplication.run( Tileserver.class, args );
		// System.out.println( "CONTEXT ::: " + ctx.getDisplayName() );
		// System.out.println( "CONTEXT ::: " + ctx.getDisplayName() );
		// System.out.println( "CONTEXT ::: " + ctx.getDisplayName() );

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
			// local properties (configuration file in project)
			if ( new File( "config.properties" ).exists() ) {
				input = new FileInputStream( "config.properties" );
				properties.load( input );
			}
			
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
