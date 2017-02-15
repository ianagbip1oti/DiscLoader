/**
 * 
 */
package io.discloader.discloader.events;

import io.discloader.discloader.DiscLoader;
import io.discloader.discloader.objects.structures.Message;

/**
 * The object passed to the {@literal "MessageCreate"} eventHandler
 * @author Perry Berman
 * @since 0.0.1_Alpha
 * @see Message
 * @see DiscLoader
 * @see EventHandler
 */
public class MessageCreateEvent {

	/**
	 * The current instance of the loader.
	 */
	public final DiscLoader loader;
	
	/**
	 * The new message received from the gateway.
	 */
	public final Message message;
	
	public MessageCreateEvent(Message message) {
		this.message = message;
		this.loader = this.message.loader;
	}

}