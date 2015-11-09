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
				System.out.println( channel_name + " " + reader.object().isDataSet( channel_name ) );
				
				if( reader.object().isDataSet( channel_name ) ) {
					channels.add( new Channel( this, channel_name ) );
				}
			}
		
			if( channels.size() == 0 ) {
				System.out.println( "No channels found!" );
			}
			
			for( Channel channel : channels ) {
				System.out.print( "Channel: " + channel + ": " );
				System.out.println( channel.getDimensions()[0] + " " + channel.getDimensions()[1] + " " + channel.getDimensions()[2] + " " );
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
	
	public IHDF5Reader getReader() {
		return reader;
	}

	
	private MDShortArray readChannelBlock( int channel, int size, int offset_z, int offset_x, int offset_y) {
		return reader.uint16().readMDArrayBlockWithOffset("c" + channel, new int[] {1,  size, size}, new long[] { offset_z, offset_x, offset_y } );
	}

		
	/** 
	 * @param channel The channel
	 * @param offset_z slice
	 * @return a flattened array of the block */
	public short[] getBlock( int channel, int size, int offset_z, int offset_x, int offset_y ) throws Exception {
		if ( channel > channels.size() - 1 ) {
			System.out.println("Channel " + channel + " not found. There are only " + channels.size() + " channels.");
			throw new Exception( "Channel " + channel + " not found. There are only " + channels.size() + " channels.");
		}
		
		MDShortArray blockC0 = readChannelBlock( channel, size, offset_z, offset_x, offset_y );
		return blockC0.getAsFlatArray();		
	}

	@Override
	public String toString() {
		return filename;
	}
}
