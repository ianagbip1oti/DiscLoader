/**
 * 
 */
package io.discloader.discloader.common.event.message;

import io.discloader.discloader.common.event.DLEvent;
import io.discloader.discloader.core.entity.message.Message;
import io.discloader.discloader.entity.channel.ITextChannel;

/**
 * @author Perry Berman
 *
 */
public class MessageDeleteEvent extends DLEvent {

	private final Message message;
	
	public MessageDeleteEvent(Message message) {
		super(message.loader);
		
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}
	
	public ITextChannel getChannel() {
		return message.channel;
	}
	
}
