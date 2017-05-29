package de.vonfelix.tileserver;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.vonfelix.tileserver.image.ImageProxy;
import de.vonfelix.tileserver.stack.IStack;
import de.vonfelix.tileserver.tile.Coordinates;
import de.vonfelix.tileserver.tile.Parameters;
import de.vonfelix.tileserver.tile.TileProxy;

@RestController
public class TileserverController {

	static Logger logger = LogManager.getLogger();

	@Autowired
	private Environment env;

	@Autowired
	ImageProxy imageProxy;

	@Autowired
	TileProxy tileProxy;

	private Integer default_tilesize;
	private Integer default_quality;


	public TileserverController() throws Exception {
		super();
	}

	@PostConstruct
	private void setDefaults() {
		default_tilesize = env.getProperty("tilebuilder.default_tilesize", Integer.class, 512);
		default_quality = env.getProperty("tilebuilder.default_quality", Integer.class, 90);
	}

	@Autowired
	ServletContext servletContext;
	
	@RequestMapping( value = "/{image_name}/{stack_name}/{slice_index:[\\d]+}/{row_index:[\\d]+}_{column_index:[\\d]+}_{scale_level:[\\d]+}", produces = MediaType.IMAGE_JPEG_VALUE )
	@ResponseBody
	public byte[] getImage( HttpServletResponse resp,
			@PathVariable( "image_name" ) String image_name,
			@PathVariable( "stack_name" ) String stack_name,
			@PathVariable( "slice_index" ) int slice_index,
			@PathVariable( "row_index" ) int row_index,
			@PathVariable( "column_index" ) int column_index,
			@PathVariable( "scale_level" ) int scale_level,
			@RequestParam(value = "tilesize", required = false) Integer tilesize,
			@RequestParam(value = "quality", required = false) Integer quality,
			@RequestParam( value = "color", required = false ) String adjCol,
			@RequestParam( value = "min", required = false ) String adjMin,
			@RequestParam( value = "max", required = false ) String adjMax,
			@RequestParam( value = "gamma", required = false ) String adjExp
			) throws Exception {

		long startTime = System.nanoTime();

		tilesize = (tilesize == null) ? default_tilesize : tilesize;
		quality = (quality == null) ? default_quality : quality;
		float q = ( (float) quality ) / 100;

		Parameters parameters = new Parameters.Builder().quality( q ).colors( adjCol ).min_values( adjMin ).max_values( adjMax ).exponents( adjExp ).build();

		Coordinates coordinates = new Coordinates(
				tilesize, scale_level, column_index, row_index, slice_index );

		logger.trace( "Request for " + image_name + "/" + stack_name + " " + coordinates + " " + parameters );

		byte[] img = tileProxy.getJpegTile( imageProxy.getImage( image_name ).getStack( stack_name ), coordinates, parameters );
		
		long duration = ( System.nanoTime() - startTime );
		resp.setHeader( "Generation-Time", duration + "" );
		logger.info( "serving tile " + image_name + "/" + stack_name + " " + coordinates + " (" + duration / 1000000 + "ms)" );
		logger.debug( "ts=" + tilesize + " q=" + quality + " size:" + img.length );
		Tileserver.countTile();

		return img;
	}

	@RequestMapping( value = "/{image_name}/{stack_name}/{slice_index:[\\d]+}/small", produces = MediaType.IMAGE_JPEG_VALUE )
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

		logger.trace( String.valueOf( imageProxy.hashCode() ) );

		IStack stack = imageProxy.getImage( image_name ).getStack( stack_name );

		// go through scale levels (from small to big) to find the first scale
		// level where either width or height is > 192 (most likely it's the
		// first scale level)
		int thumbnailSize = env.getProperty( "tilebuilder.thumbnail_size", Integer.class );
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
			value = { "/{image_name}/project.yaml", "/{image_name}.yaml" }, produces = "text/yaml")
	public String getYaml( HttpServletResponse resp,
			@PathVariable( "image_name" ) String image_name,
			HttpServletRequest req
			) {

		return (String) imageProxy.getImage( image_name ).getConfigurationYaml();

	}

	@RequestMapping(method = RequestMethod.GET, value = { "/list" }, produces = "text/yaml")
	@ResponseBody
	public String getList( HttpServletResponse resp, HttpServletRequest req ) {
		return imageProxy.getList();
	}

}
