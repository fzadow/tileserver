package de.vonfelix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class TileProxy {

	private File tileDir;

	private TileGenerator tileGenerator = new TileGenerator();

	public TileProxy() {
		System.out.println( "TileProxy: initializing" );
		if ( Tileserver.hasProperty( "tile_dir" ) ) {
			if ( Files.isDirectory( Paths.get( Tileserver.getProperty( "tile_dir" ) ) ) ) {
				tileDir = new File( Tileserver.getProperty( "tile_dir" ) );
				System.out.println( "TileProxy: Found tile_dir: " + Tileserver.getProperty( "tile_dir" ) );
			}
		}
	}

	public byte[] getJpegTile( IStack stack, TileCoordinates coordinates ) throws Exception {

		// check if tile exists on disk
		if ( tileDir != null ) {
			System.out.println( "TileProxy: checking tile_dir for existing file" );
			Path path = Paths.get( tileDir.getAbsolutePath(), stack.getHdf5Image().getName(), stack.getName(),
					String.valueOf( coordinates.getSliceIndex() ),
					String.valueOf( coordinates.getRowIndex() ) + "_" + String.valueOf( coordinates.getColumnIndex() )
							+ "_" + String.valueOf( coordinates.getScaleLevel() ) + ".jpg" );
			if ( Files.isReadable( path ) ) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Files.copy( path, baos );
				System.out.println( "TileProxy: Tile found in " + tileDir.getAbsolutePath() + "." );
				return baos.toByteArray();

			}
		}

		// get tile from tile generator
		BufferedImage tile = tileGenerator.getTile( stack, coordinates );

		// return as JPEG
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( tile, "jpg", baos );
		return baos.toByteArray();
	};
}
