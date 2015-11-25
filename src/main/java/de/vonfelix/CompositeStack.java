package de.vonfelix;

import java.util.HashMap;

public class CompositeStack extends AbstractStack {
	
	HashMap<String, Channel> channels = new HashMap<String, Channel>();;
	
	public CompositeStack( HDF5Image hdf5Image, String name, String title) {
		super( hdf5Image, name, title );
	}
	
	public void addChannel( Channel channel ) {
		channels.put( channel.getStack().getId(), channel );
	}
	
	public HashMap<String, Channel> getChannels() {
		return channels;
	}


	@Override
	public long[] getDimensions( int scaleLevel ) {
		try {
			return channels.values().iterator().next().getStack().getDimensions( scaleLevel );
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
