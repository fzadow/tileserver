package de.vonfelix.tileserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import de.vonfelix.tileserver.image.ImageProxy;
import de.vonfelix.tileserver.stack.IStack;
import de.vonfelix.tileserver.tile.Coordinates;
import de.vonfelix.tileserver.tile.Parameters;
import de.vonfelix.tileserver.tile.TileProxy;

@RestController
public class TileserverController {

	static Logger logger = LogManager.getLogger();

	private ImageProxy imageProxy;
	private TileProxy tileProxy;

	public TileserverController() throws Exception {
		super();
		this.imageProxy = ImageProxy.getInstance();
		this.tileProxy = TileProxy.getInstance();
	}

	@Autowired
	ServletContext servletContext;
	
	@RequestMapping( value = "/{image_name}/{stack_name}/{slice_index:[\\d]+}/{row_index:[\\d]+}_{column_index:[\\d]+}_{scale_level:[\\d]+}" )
	@ResponseBody
	public byte[] getImage( HttpServletResponse resp,
			@PathVariable( "image_name" ) String image_name,
			@PathVariable( "stack_name" ) String stack_name,
			@PathVariable( "slice_index" ) int slice_index,
			@PathVariable( "row_index" ) int row_index,
			@PathVariable( "column_index" ) int column_index,
			@PathVariable( "scale_level" ) int scale_level,
			@RequestParam( value = "tilesize", required = false, defaultValue = "512" ) int tilesize,
			@RequestParam( value = "quality", required = false, defaultValue = "90" ) int quality,
			@RequestParam( value = "color", required = false ) String adjCol,
			@RequestParam( value = "min", required = false ) String adjMin,
			@RequestParam( value = "max", required = false ) String adjMax,
			@RequestParam( value = "gamma", required = false ) String adjExp
			) throws Exception {
		
		long startTime = System.nanoTime();

		float q = ( (float) quality ) / 100;

		Parameters parameters = new Parameters.Builder().quality( q ).colors( adjCol ).min_values( adjMin ).max_values( adjMax ).exponents( adjExp ).build();

		Coordinates coordinates = new Coordinates(
				tilesize, scale_level, column_index, row_index, slice_index );

		logger.trace( "Request for " + image_name + "/" + stack_name + " " + coordinates + " " + parameters );

		byte[] img = tileProxy.getJpegTile( imageProxy.getImage( image_name ).getStack( stack_name ), coordinates, parameters );
		
		long duration = ( System.nanoTime() - startTime );
		resp.setHeader( "Content-Disposition", "inline" );
		resp.setContentType( "image/jpg" );
		resp.setHeader( "Generation-Time", duration + "" );
		logger.info( "serving tile " + image_name + "/" + stack_name + " " + coordinates + " (" + duration / 1000000 + "ms)" );
		logger.debug( "ts=" + tilesize + " q=" + quality + " size:" + img.length );
		Tileserver.countTile();

		return img;
	}

	@RequestMapping( value = "/{image_name}/{stack_name}/{slice_index:[\\d]+}/small" )
	@ResponseBody
	public byte[] getImage( HttpServletResponse resp, @PathVariable( "image_name" ) String image_name,
			@PathVariable( "stack_name" ) String stack_name,
			@PathVariable( "slice_index" ) int slice_index,
			@RequestParam( value = "color", required = false ) String adjCol,
			@RequestParam( value = "min", required = false ) String adjMin,
			@RequestParam( value = "max", required = false ) String adjMax,
			@RequestParam( value = "gamma", required = false ) String adjExp)
					throws Exception {

		long startTime = System.nanoTime();
		logger.debug( "small.jpg for " + image_name + "/" + stack_name + " slice index " + slice_index );

		resp.setHeader( "Content-Disposition", "inline" );
		resp.setContentType( "image/jpg" );

		logger.trace( String.valueOf( imageProxy.hashCode() ) );

		IStack stack = imageProxy.getImage( image_name ).getStack( stack_name );

		// go through scale levels (from small to big) to find the first scale
		// level where either width or height is > 192 (most likely it's the
		// first scale level)
		int thumbnailSize = Tileserver.hasProperty( "thumbnail_size" ) ? Integer.parseInt( Tileserver.getProperty( "thumbnail_size" ) ) : 192;
		int stackWidth, thumbnailWidth, stackHeight, thumbnailHeight;
		for ( int scaleLevel = stack.getScaleLevels() - 1; scaleLevel >= 0; --scaleLevel ) {
			stackWidth = thumbnailWidth = (int) stack.getDimensions( scaleLevel )[ 2 ];
			stackHeight = thumbnailHeight = (int) stack.getDimensions( scaleLevel )[ 1 ];

			if ( thumbnailWidth > thumbnailSize || thumbnailHeight > thumbnailSize ) {
				if ( thumbnailWidth > thumbnailHeight ) {
					thumbnailHeight = ( thumbnailHeight * thumbnailSize ) / thumbnailWidth;
					thumbnailWidth = thumbnailSize;
				} else {
					thumbnailWidth = ( thumbnailWidth * thumbnailSize ) / thumbnailHeight;
					thumbnailHeight = thumbnailSize;
				}
				logger.trace( "Request for thumbnail with dimensions: " + thumbnailWidth + "x" + thumbnailHeight );

				Parameters parameters = new Parameters.Builder().colors( adjCol ).min_values( adjMin ).max_values( adjMax ).dimensions( thumbnailWidth, thumbnailHeight ).build();
				Coordinates coordinates = new Coordinates( stackWidth, stackHeight, scaleLevel, 0, 0, slice_index );
				logger.debug( "getting thumbnail tile at " + coordinates );

				byte[] img = tileProxy.getJpegTile( stack, coordinates, parameters, thumbnailWidth, thumbnailHeight );

				return img;
			}
		}
		return null;
	}

	@RequestMapping( method=RequestMethod.GET,
			value = {"/{image_name}/project.yaml", "/{image_name}.yaml"} )
	@ResponseBody
	public void getYaml( HttpServletResponse resp,
			@PathVariable( "image_name" ) String image_name,
			HttpServletRequest req
			) {
		
		// get request file name from URL
		String[] url = req.getAttribute( HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE ).toString().split("/");
		// set response file name to request file name (either project.yaml or {image_name}.yaml
		resp.setHeader("Content-Disposition", "attachment;filename=" + url[ url.length - 1 ] );
		
		logger.debug( "YAML request for {}.", image_name );
		
		try {
			InputStream is = new FileInputStream( new File( Tileserver.getProperty("source_image_dir") + image_name + ".yaml" ) );
			org.apache.commons.io.IOUtils.copy(is, resp.getOutputStream() );
			resp.flushBuffer();
		} catch ( FileNotFoundException e ) {
			logger.error( "File {}{}.yaml not found.", Tileserver.getProperty("source_image_dir"), image_name );
		} catch (IOException e ) {
			logger.error( "Error writing file to output stream ({})", image_name );
		}

	}

	@RequestMapping( method = RequestMethod.GET, value = { "/list" }, produces = "application/yaml" )
	@ResponseBody
	public String getList( HttpServletResponse resp, HttpServletRequest req ) {
		return imageProxy.getList();
	}

}
