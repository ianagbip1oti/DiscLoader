package io.discloader.discloader.common.event.message;

import io.discloader.discloader.core.entity.channel.PrivateChannel;
import io.discloader.discloader.core.entity.message.Message;

public class PrivateMessageDeleteEvent extends MessageDeleteEvent {
	public PrivateMessageDeleteEvent(Message message) {
		super(message);
	}

	@Override
	public PrivateChannel getChannel() {
		return (PrivateChannel) super.getChannel();
	}
}
