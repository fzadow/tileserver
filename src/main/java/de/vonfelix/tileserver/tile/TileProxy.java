package de.vonfelix.tileserver.tile;
import static de.vonfelix.tileserver.tile.Parameters.Parameter.QUALITY;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;

import de.vonfelix.tileserver.Tileserver;
import de.vonfelix.tileserver.stack.IStack;

public class TileProxy {

	private File tileDir;
	private File writableTileDir;
	private boolean bDiskRead;
	private boolean bDiskWrite;

	JPEGImageWriteParam jpegParams = new JPEGImageWriteParam( null );

	private TileGenerator tileGenerator = new TileGenerator();
	private TileLog tileLog = TileLog.getInstance();

	static Logger logger = LogManager.getLogger( TileProxy.class.getName() );

	private static TileProxy instance;

	public static synchronized TileProxy getInstance() {
		if ( TileProxy.instance == null ) {
			TileProxy.instance = new TileProxy();
		}
		return TileProxy.instance;
	}

	// TODO also try reading files from writable_tile_dir if possible

	private TileProxy() {
		if ( Tileserver.hasProperty( "tile_dir" ) ) {
			if ( Files.isDirectory( Paths.get( Tileserver.getProperty( "tile_dir" ) ) ) ) {
				tileDir = new File( Tileserver.getProperty( "tile_dir" ) );
				logger.info( "Using tile_dir: " + Tileserver.getProperty( "tile_dir" ) );
			}
		}
		if ( Tileserver.hasProperty( "writable_tile_dir" ) ) {
			if ( Files.isWritable( Paths.get( Tileserver.getProperty( "writable_tile_dir" ) ) ) && Files.isDirectory( Paths.get( Tileserver.getProperty( "writable_tile_dir" ) ) ) ) {
				writableTileDir = new File( Tileserver.getProperty( "writable_tile_dir" ) );
				logger.info( "Using writable tile_dir: " + Tileserver.getProperty( "writable_tile_dir" ) );
			} else {
				logger.error( "writable_tile_dir: " + Tileserver.getProperty( "writable_tile_dir" ) + " is not writable (or not a directory" );
			}
		}

		this.bDiskRead = tileDir != null && tileDir.isDirectory() && Tileserver.hasProperty( "read_from_disk" ) && Boolean.parseBoolean( Tileserver.getProperty( "read_from_disk" ) );
		this.bDiskWrite = writableTileDir != null && writableTileDir.isDirectory() && Tileserver.hasProperty( "save_to_disk" ) && Boolean.parseBoolean( Tileserver.getProperty( "save_to_disk" ) );

		jpegParams.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
		jpegParams.setCompressionQuality( 0.0f );

	}

	/**
	 * get a JPEG tile at the given coordinates with the given parameters
	 * 
	 * @param stack
	 * @param coordinates
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public byte[] getJpegTile( IStack stack, Coordinates coordinates, Parameters parameters ) throws Exception {
		return getJpegTile( stack, coordinates, parameters, 0, 0 );
	}

	/**
	 * get a JPEG tile at the given coordinates
	 * 
	 * @param stack
	 * @param coordinates
	 * @return
	 * @throws Exception
	 */
	public byte[] getJpegTile( IStack stack, Coordinates coordinates ) throws Exception {
		return getJpegTile( stack, coordinates, null, 0, 0 );
	}

	/**
	 * get a scaled (width x height) JPEG tile
	 * 
	 * @param stack
	 * @param coordinates
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public byte[] getJpegTile( IStack stack,
			Coordinates coordinates,
			Parameters parameters,
			int width,
			int height ) throws Exception {

		logger.debug( "getting tile for " + stack + " at " + coordinates );

		// check if tile exists on disk
		if ( bDiskRead ) {
			Path path = Paths.get( tileDir.getAbsolutePath(), stack.getImage().getName(), stack.getId(),
					String.valueOf( coordinates.getSliceIndex() ),
					String.valueOf( coordinates.getRowIndex() ) + "_" + String.valueOf( coordinates.getColumnIndex() )
							+ "_" + String.valueOf( coordinates.getScaleLevel() ) + ".jpg" );
			if ( Files.isReadable( path ) ) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Files.copy( path, baos );
				logger.debug( "found file for " + coordinates );
				return baos.toByteArray();
			}
		}

		// get tile from tile generator
		Tile tile = tileGenerator.getTile( stack, coordinates, parameters );

		BufferedImage image = tile.getImage();

		// if width was specified, scale image (down) to width*height.
		if ( width > 0 ) {
			long startTime = System.nanoTime();
			logger.debug( "scaling image from " + coordinates.getWidth() + "x" + coordinates.getHeight() + " to " + width + "x" + height );
			image = Scalr.resize( image, Scalr.Method.BALANCED, width, height );
			logger.trace( "scaling took " + ( System.nanoTime() - startTime ) / 1000000 + "ms" );
		}

		// get as JPEG
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// ImageIO.write( image, "jpg", baos );
		Float jpegQuality = ( parameters.has( QUALITY ) && parameters.<Float> get( QUALITY ) != null )
				? parameters.<Float> get( QUALITY ) : 1f;

		jpegParams.setCompressionQuality( jpegQuality );
		ImageWriter writer = ImageIO.getImageWritersByFormatName( "jpg" ).next();
		writer.setOutput( new MemoryCacheImageOutputStream( baos ) );
		writer.write( null, new IIOImage( image, null, null ), jpegParams );
		baos.flush();

		// save to disk
		if ( bDiskWrite ) {
			long startTime = System.nanoTime();
			File outFile = new File( writableTileDir.getAbsolutePath() + "/" + stack.getImage().getName() + "/" + stack.getId() + "/" + String.valueOf( coordinates.getSliceIndex() ) + "/" + String.valueOf( coordinates.getRowIndex() ) + "_" + String.valueOf( coordinates.getColumnIndex() ) + "_" + String.valueOf( coordinates.getScaleLevel() ) + ".jpg" );
			outFile.getParentFile().mkdirs();
			ImageIO.write( image, "jpg", outFile );
			logger.debug( "saved file (" + ( ( System.nanoTime() - startTime ) / 1000000 ) + "ms)" );
		}

		return baos.toByteArray();
	};
}
