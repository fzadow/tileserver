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
	
	private TileGenerator tileGenerator;
	private HDF5Image hdf5Image;
	private static final String FILENAME = "/home/felix/Dev/tileserver/ovary.h5";

	
	public TileserverController() throws Exception {
		super();
		this.hdf5Image = new HDF5Image( FILENAME );
		this.tileGenerator = new TileGenerator(hdf5Image);
	}

	@RequestMapping("/")
	public String test() {
		return "ok";
	}

	@Autowired
	ServletContext servletContext;
	
	@RequestMapping(value = "/name-{channel}/{slice_index:[\\d]+}/{row_index}_{column_index}_{scale_level}", produces = "image/jpg" )
	@ResponseBody
	public byte[] getImage( HttpServletResponse resp,
			@PathVariable("channel") String channel,
			@PathVariable("slice_index") int slice_index,
			@PathVariable("row_index") int row_index,
			@PathVariable("column_index") int column_index,
			@PathVariable("scale_level") int scale_level
			) throws Exception {
		
		resp.reset();
		resp.setHeader("Content-Disposition", "inline");
		resp.setContentType("image/jpg");
		
		TileCoordinates tc = new TileCoordinates(hdf5Image, 256, row_index, column_index, slice_index);

		System.out.println( "getting tile " + tc + ", scale " + scale_level );
		
		byte[] img = tileGenerator.getTileAsJPEG( hdf5Image.getChannel( channel ), tc );
		return img;
	}
	
	@ResponseStatus( value=HttpStatus.CONFLICT, reason="problem with HDF5 source file" )
	@ExceptionHandler( Exception.class )
	public void conflict() {
		
	}

}
