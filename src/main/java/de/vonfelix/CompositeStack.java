package de.vonfelix;

import java.util.Collection;
import java.util.HashMap;

public class CompositeStack extends AbstractStack {
	
	HashMap<String, Channel> channels = new HashMap<String, Channel>();;
	
	public CompositeStack( AbstractImage image, String name, String title ) {
		super( image, name, title );
	}
	
	public void addChannel( Channel channel ) {
		channels.put( channel.getStack().getId(), channel );
	}
	
	public Collection<Channel> channels() {
		return channels.values();
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
}
