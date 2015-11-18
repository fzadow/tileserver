package de.vonfelix;

public class TileProxy  {

	private static TileGenerator tileGenerator = new TileGenerator();

	public static byte[] getTileAsJPEG( IStack stack, int scaleLevel, TileCoordinates coordinates ) throws Exception {
		
		return tileGenerator.getTileAsJPEG( stack, scaleLevel, coordinates );
	};
}
