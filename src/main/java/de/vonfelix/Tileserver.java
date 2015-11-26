package de.vonfelix;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	public static synchronized void log( String message ) {
		String className = Thread.currentThread().getStackTrace()[ 2 ].getClassName();
		className = className.substring( className.lastIndexOf( "." ) + 1 );
		message = className + " : " + message;

		if ( Boolean.parseBoolean( properties.getProperty( "debug_flush_log" ) ) ) {
			System.out.println( message );
		} else {
			if ( log.containsKey( Thread.currentThread().getId() ) ) {
				log.get( Thread.currentThread().getId() ).add( message );
			} else {
				startTimes.put( Thread.currentThread().getId(), System.nanoTime() );
				log.put( Thread.currentThread().getId(), new ArrayList<>( Arrays.asList( message ) ) );
			}
		}
	}

	public static synchronized void finishLog( long threadId ) {
		if ( !Boolean.parseBoolean( properties.getProperty( "debug_flush_log" ) ) ) {
			System.out.println();
			for ( String message : log.remove( threadId ) ) {
				System.out.println( message );
			}
			System.out.println( " -- time: " + ( ( System.nanoTime() - startTimes.remove( threadId ) ) / 1000000 ) + "ms" );
		}
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
