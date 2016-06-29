package de.vonfelix.tileserver.tile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

public class Tile implements Comparable<Tile> {

	BufferedImage image;
	Coordinates coordinates;
	Parameters parameters;
	final ImageWriter writer = ImageIO.getImageWritersByFormatName( "jpg" ).next();
	JPEGImageWriteParam jpegParams = new JPEGImageWriteParam( null );

	public Tile( BufferedImage image, Coordinates coordinates, Parameters parameters ) {
		this.image = image;
		this.coordinates = coordinates;
		this.parameters = parameters;

		jpegParams.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
		jpegParams.setCompressionQuality( 0.0f );
	}

	public BufferedImage getImage() {
		return image;
	}

	public Coordinates getCoordinates() {
		return coordinates;
	}

	public Parameters getParameters() {
		return parameters;
	}

	public byte[] asJPEG() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer.setOutput( baos );
		writer.write( null, new IIOImage( image, null, null ), jpegParams );

		//		ImageIO.write( image, "jpg", baos );
		return baos.toByteArray();
	}

	@Override
	public int compareTo( Tile tile ) {
		if ( tile.getImage().equals( image ) && tile.getCoordinates().equals( coordinates ) && tile.getParameters().equals( parameters ) ) {
			return 0;
		}
		return -1;
	}

}
