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
	
	// TODO mapping for small.jpg

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
		Tileserver.log( "Duration for " + image_name + " " + stack_name + " " + slice_index + " " + row_index + " " + column_index + " " + scale_level + "  =  " + duration / 1000000 + " ms" );
		return img;
	}
	
	@ResponseStatus( value=HttpStatus.CONFLICT, reason="problem with HDF5 source file" )
	@ExceptionHandler( Exception.class )
	public void conflict() {
		
	}
}
