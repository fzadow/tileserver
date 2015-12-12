package de.vonfelix.tileserver.tile;

public class Tile {

	short[] tile;

	Coordinates coordinates;
	int width, height;

	public Tile( short[] tileArray, int width, int height ) {
		this.tile = tileArray;
		this.width = width;
		this.height = height;
	}

	public short[] getTile() {
		return tile;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
