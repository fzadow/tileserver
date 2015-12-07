package de.vonfelix.tileserver;

import java.util.Collection;
import java.util.LinkedHashMap;

public class CompositeStack extends AbstractStack {
	
	LinkedHashMap<String, Channel> channels = new LinkedHashMap<>();
	
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

	public int numberOfChannels() {
		return channels.size();
	}

	@Override
	public int getScaleLevels() {
		// TODO Auto-generated method stub
		return super.getScaleLevels();
	}
}
