package de.vonfelix;

import java.util.ArrayList;

import ch.systemsx.cisd.base.mdarray.MDShortArray;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

public class HDF5Image {


	private String filename;
	
	private	IHDF5Reader reader;
	private ArrayList<Channel> channels;
	
	int SIZE = 256;
	
	public HDF5Image( String filename ){
		this.filename= filename;
		
		// open HDF5 file
		reader = HDF5Factory.openForReading( filename );
		
		System.out.println( reader.object().getAllAttributeNames("c0") );
		System.out.println( reader.object().getDimensions("c0")[0] );
		
	}
	
	/** an {@link ArrayList} with all the {@link Channel}s in the image*/
	private ArrayList<Channel> getChannels() {
		if( channels == null ) {
			channels = new ArrayList<Channel>();
		
			// assume all top-level datasets are channels
			for( String channel_name : reader.object().getAllGroupMembers("/") ) {
				if( reader.object().isDataSet( channel_name ) ) {
					channels.add( new Channel( this, channel_name ) );
				}
			}
		
			if( channels.size() == 0 ) {
				System.out.println( "No channels found!" );
			}
		}
		
		return channels;
	}
	
	public int getNumChannels() {
		return getChannels().size();
	}
	
	public Channel getChannel( int n ) {
		return getChannels().get( n );
	}
	
	public Channel getChannel( String name ) {
		for( Channel channel : getChannels() ) {
			if( channel.getName().equals( name ) ) {
				return channel;
			}
		}
		return null;
	}
	
	public IHDF5Reader getReader() {
		return reader;
	}

	
	@Override
	public String toString() {
		return filename;
	}
}
