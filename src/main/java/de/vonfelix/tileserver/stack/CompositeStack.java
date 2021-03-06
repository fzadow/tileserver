package de.vonfelix.tileserver.stack;

import java.util.Collection;
import java.util.LinkedHashMap;

import de.vonfelix.tileserver.image.AbstractImage;

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

	public Channel getChannel( int index ) {
		return (Channel) channels.values().toArray()[ index ];
	}

	@Override
	public int getScaleLevels() {
		// TODO Auto-generated method stub
		return super.getScaleLevels();
	}
}
