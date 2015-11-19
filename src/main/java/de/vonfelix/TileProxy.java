package de.vonfelix;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class TileProxy  {

	private static TileGenerator tileGenerator = new TileGenerator();

	public static byte[] getJpegTile( IStack stack, TileCoordinates coordinates ) throws Exception {
		
		
		// get tile from tile generator
		BufferedImage tile = tileGenerator.getTile( stack, coordinates );

		// return as JPEG
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( tile, "jpg", baos);
		return baos.toByteArray();
	};
}
