package de.vonfelix;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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
		if( properties == null )
			initProperties();
		
		return properties.getProperty( key );
	}
	
	static boolean hasProperty( String key ) {
		if( properties == null )
			initProperties();
		
		return properties.containsKey( key );
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
			input = new FileInputStream( "config.properties" );
			properties.load( input );
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
