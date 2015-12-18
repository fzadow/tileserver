package de.vonfelix.tileserver.tile;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class TileLog {

	private class LogEntry {
		Coordinates coordinates;
		Parameters parameters;
		LocalDate date;

		public LogEntry( Coordinates coordinates, Parameters parameters ) {
			this.coordinates = coordinates;
			this.parameters = parameters;
			this.date = LocalDate.now();
		}

		public Period age() {
			return Period.between( date, LocalDate.now() );
		}
	}

	private static TileLog instance;
	private ArrayList<LogEntry> entries;

	private TileLog() {
	};

	public static synchronized TileLog getInstance() {
		if ( TileLog.instance == null ) {
			TileLog.instance = new TileLog();
		}

		return TileLog.instance;
	}

	public void log( Tile tile ) {
		entries.add( new LogEntry( tile.coordinates, tile.parameters ) );
	}

}
