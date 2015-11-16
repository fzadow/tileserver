package de.vonfelix;

import de.vonfelix.ChannelColor.ColorName;

import java.util.ArrayList;
import java.util.HashMap;

public class CompositeStack extends AbstractStack {
	
	HashMap<String, Stack> channels = new HashMap<String, Stack>();;
	HashMap<String, ColorName> channelColors = new HashMap<String, ColorName>();
	
	public CompositeStack( HDF5Image hdf5Image, String name, String title) {
		super( hdf5Image, name, title );
	}
	
	public void addChannel( Stack channel, ColorName channelColor ) {
		channels.put( channel.getName(), channel );
		channelColors.put(  channel.getName(), channelColor );
	}
	
	public HashMap<String, Stack> getChannels() {
		return channels;
	}
	
	public ColorName getColor( String channelName ) {
		return channelColors.get( channelName );
	}
	

	@Override
	public long[] getDimensions( int scaleLevel ) {
		try {
			return channels.values().iterator().next().getDimensions( scaleLevel );
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getNumScaleLevels() {
		// TODO Auto-generated method stub
		return 0;
	}

}
