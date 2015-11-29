package de.vonfelix;

import java.util.Collection;
import java.util.HashMap;

public class CompositeStack extends AbstractStack {
	
	HashMap<String, Channel> channels = new HashMap<String, Channel>();
	
	public CompositeStack( AbstractImage image, String name, String title ) {
		super( image, name, title );
	}
	
	public void addChannel( Channel channel ) {

		// if this is the first channel added, set scaleLevel and
		if ( channels.isEmpty() ) {
			this.scaleLevels = channel.getStack().getScaleLevels();
			this.dimensions = channel.getStack().getDimensions();
		}

		// TODO make sure all added channels have the same dimensions

		channels.put( channel.getStack().getId(), channel );
	}
	
	public Collection<Channel> channels() {
		return channels.values();
	}

	@Override
	public int getScaleLevels() {
		// TODO Auto-generated method stub
		return super.getScaleLevels();
	}
}
