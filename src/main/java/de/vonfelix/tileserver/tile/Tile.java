package de.vonfelix.tileserver.tile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tile implements Comparable<Tile> {

	BufferedImage image;
	Coordinates coordinates;
	Parameters parameters;

	public Tile( BufferedImage image, Coordinates coordinates, Parameters parameters ) {
		this.image = image;
		this.coordinates = coordinates;
		this.parameters = parameters;
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
		ImageIO.write( image, "jpg", baos );
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
