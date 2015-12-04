package de.vonfelix;

import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	@RequestMapping( value = "/{image_name}-{stack_name}/{slice_index:[\\d]+}/{row_index:[\\d]+}_{column_index:[\\d]+}_{scale_level:[\\d]+}" )
	@ResponseBody
	public byte[] getImage( HttpServletResponse resp,
			@PathVariable( "image_name" ) String image_name,
			@PathVariable( "stack_name" ) String stack_name,
			@PathVariable( "slice_index" ) int slice_index,
			@PathVariable( "row_index" ) int row_index,
			@PathVariable( "column_index" ) int column_index,
			@PathVariable( "scale_level" ) int scale_level,
			@RequestParam( value = "colors", required = false ) String adjCol,
			@RequestParam( value = "min_vals", required = false ) String adjMin,
			@RequestParam( value = "max_vals", required = false ) String adjMax,
			@RequestParam( value = "exponents", required = false ) String adjExp
			) throws Exception {
		
		long startTime = System.nanoTime();
		
		Tileserver.log( image_name + " " + stack_name + " " + slice_index + " " + row_index + " " + column_index + " " + scale_level );
		
		resp.setHeader("Content-Disposition", "inline");
		resp.setContentType("image/jpg");
		
		TileParameters parameters = parseParameters( adjCol, adjMin, adjMax, adjExp );
		TileCoordinates coordinates = new TileCoordinates( 512, scale_level, column_index, row_index, slice_index );

		Tileserver.log( "getting tile @ " + coordinates + " with parameters " );
		
		byte[] img = tileProxy.getJpegTile( imageHandler.getImage( image_name ).getStack( stack_name ), coordinates, parameters );
		
		long duration = ( System.nanoTime() - startTime );
		resp.setHeader( "Generation-Time", duration + "" );
		Tileserver.log( "Duration for " + image_name + " " + stack_name + " " + slice_index + " " + row_index + " " + column_index + " " + scale_level + "  =  " + duration / 1000000 + " ms" );

		return img;
	}

	@RequestMapping( value = "/{image_name}-{stack_name}/{slice_index:[\\d]+}/small" )
	@ResponseBody
	public byte[] getImage( HttpServletResponse resp, @PathVariable( "image_name" ) String image_name,
			@PathVariable( "stack_name" ) String stack_name,
			@PathVariable( "slice_index" ) int slice_index,
			@RequestParam( value = "colors", required = false ) String adjCol,
			@RequestParam( value = "min_vals", required = false ) String adjMin,
			@RequestParam( value = "max_vals", required = false ) String adjMax,
			@RequestParam( value = "exponents", required = false ) String adjExp)
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

				TileParameters parameters = parseParameters( adjCol, adjMin, adjMax, adjExp );
				TileCoordinates coordinates = new TileCoordinates( stackWidth, stackHeight, scaleLevel, 0, 0, slice_index );
				Tileserver.log( "getting thumbnail tile at " + coordinates );

				byte[] img = tileProxy.getJpegTile( stack, coordinates, parameters, thumbnailWidth, thumbnailHeight );

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
	
	private TileParameters parseParameters( String adjCol, String adjMin, String adjMax, String adjExp ) {
		ArrayList<Color> colors = new ArrayList<>();
		ArrayList<Integer> min_values = new ArrayList<>();
		ArrayList<Integer> max_values = new ArrayList<>();
		ArrayList<Double> exponents = new ArrayList<>();

		// insert LAMBDAs here... (?)

		if ( adjCol != null ) {
			for ( String e : adjCol.split( "\\s*,\\s*" ) ) {
				switch ( e.toLowerCase() ) {
				case "red":
				case "r":
					colors.add( Color.RED );
					break;
				case "green":
				case "g":
					colors.add( Color.GREEN );
					break;
				case "blue":
				case "b":
					colors.add( Color.BLUE );
					break;
				case "cyan":
				case "c":
					colors.add( Color.CYAN );
					break;
				case "magenta":
				case "m":
					colors.add( Color.MAGENTA );
					break;
				case "yellow":
				case "y":
					colors.add( Color.YELLOW );
					break;
				case "grays":
				case "gray":
				case "greys":
				case "grey":
				case "whites":
				case "white":
				case "w":
					colors.add( Color.GRAYS );
					break;
				}
			}
		}
		if ( adjMin != null ) {
			for ( String e : adjMin.split( "\\s*,\\s*" ) ) {
				min_values.add( Integer.parseInt( e ) );
			}
		}
		if ( adjMax != null ) {
			for ( String e : adjMax.split( "\\s*,\\s*" ) ) {
				max_values.add( Integer.parseInt( e ) );
			}
		}
		if ( adjExp != null ) {
			for ( String e : adjExp.split( "\\s*,\\s*" ) ) {
				exponents.add( Double.parseDouble( e ) );
			}
		}

		Tileserver.log( "parameters: " + colors + ", " + min_values + ", " + max_values + ", " + exponents );

		return new TileParameters( colors.toArray( new Color[ colors.size() ] ),
				min_values.toArray( new Integer[ min_values.size() ] ),
				max_values.toArray( new Integer[ max_values.size() ] ),
				exponents.toArray( new Double[ exponents.size() ] ) );
	}

	@ResponseStatus( value=HttpStatus.CONFLICT, reason="problem with HDF5 source file" )
	@ExceptionHandler( Exception.class )
	public void conflict() {
		
	}
}
