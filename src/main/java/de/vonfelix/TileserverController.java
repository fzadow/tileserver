package de.vonfelix;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TileserverController {

	private ImageHandler imageHandler;
	private TileProxy tileProxy;

	public TileserverController() throws Exception {
		super();
		this.imageHandler = new ImageHandler();
		this.tileProxy = new TileProxy();
	}

	@Autowired
	ServletContext servletContext;
	
	@RequestMapping( value = "/{image_name}-{stack_name}/{slice_index:[\\d]+}/{row_index}_{column_index}_{scale_level:[\\d]+}" )
	@ResponseBody
	public byte[] getImage( HttpServletResponse resp,
			@PathVariable("image_name") String image_name,
			@PathVariable("stack_name") String stack_name,
			@PathVariable("slice_index") int slice_index,
			@PathVariable("row_index") int row_index,
			@PathVariable("column_index") int column_index,
			@PathVariable("scale_level") int scale_level
			) throws Exception {
		
		long startTime = System.nanoTime();
		Tileserver.log( image_name + " " + stack_name + " " + slice_index + " " + row_index + " " + column_index + " " + scale_level );
		
		resp.setHeader("Content-Disposition", "inline");
		resp.setContentType("image/jpg");
		
		TileCoordinates coordinates = new TileCoordinates( 512, scale_level, column_index, row_index, slice_index );

		Tileserver.log( "getting tile at " + coordinates );
		
		byte[] img = tileProxy.getJpegTile( imageHandler.getImage( image_name ).getStack( stack_name ), coordinates );
		long duration = ( System.nanoTime() - startTime );
		resp.setHeader( "Generation-Time", duration + "" );
		Tileserver.log( "Duration for " + image_name + " " + stack_name + " " + slice_index + " " + row_index + " " + column_index + " " + scale_level + "  =  " + duration / 1000000 + " ms" );
		return img;
	}

	@RequestMapping( value = "/{image_name}-{stack_name}/{slice_index:[\\d]+}/small" )
	@ResponseBody
	public byte[] getImage( HttpServletResponse resp, @PathVariable( "image_name" ) String image_name,
			@PathVariable( "stack_name" ) String stack_name, @PathVariable( "slice_index" ) int slice_index)
					throws Exception {

		long startTime = System.nanoTime();
		Tileserver.log( "small.jpg for " + image_name + "/" + stack_name + " slice index " + slice_index );

		resp.setHeader( "Content-Disposition", "inline" );
		resp.setContentType( "image/jpg" );

		Tileserver.log( String.valueOf( imageHandler.hashCode() ) );

		IStack stack = imageHandler.getImage( image_name ).getStack( stack_name );

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
				System.out.println( "thumbnail dimensions: " + thumbnailWidth + "x" + thumbnailHeight );

				TileCoordinates coordinates = new TileCoordinates( stackWidth, stackHeight, scaleLevel, 0, 0, slice_index );
				Tileserver.log( "getting thumbnail tile at " + coordinates );

				byte[] img = tileProxy.getJpegTile( stack, coordinates, thumbnailWidth, thumbnailHeight );

				// TODO scale down to 192

				return img;
			}
		}

		// byte[] img = tileProxy.getJpegTile( imageHandler.getImage( image_name
		// ).getStack( stack_name ), coordinates );
		// long duration = ( System.nanoTime() - startTime );
		// Tileserver.log( "Duration for " + image_name + " " + stack_name + " "
		// + slice_index + " " + row_index + " " + column_index + " " +
		// scale_level + " = " + duration / 1000000 + " ms" );
		// return img;
		return null;
	}
	
	@ResponseStatus( value=HttpStatus.CONFLICT, reason="problem with HDF5 source file" )
	@ExceptionHandler( Exception.class )
	public void conflict() {
		
	}
}
