package de.vonfelix;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TileserverTestApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(TileserverTestApplication.class, args);
        
        //System.out.println( "Beans provided by Spring boot: " );
        
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort( beanNames );
        
        for( String beanName : beanNames ) {
        //	System.out.println( beanName );
        }
        
    }
}
