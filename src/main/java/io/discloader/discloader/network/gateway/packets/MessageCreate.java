/**
 * 
 */
package io.discloader.discloader.network.gateway.packets;

import io.discloader.discloader.client.command.CommandHandler;
import io.discloader.discloader.common.event.message.GuildMessageCreateEvent;
import io.discloader.discloader.common.event.message.MessageCreateEvent;
import io.discloader.discloader.common.event.message.PrivateMessageCreateEvent;
import io.discloader.discloader.core.entity.message.Message;
import io.discloader.discloader.entity.channel.ITextChannel;
import io.discloader.discloader.entity.message.IMessage;
import io.discloader.discloader.network.gateway.DiscSocket;
import io.discloader.discloader.network.json.MessageJSON;
import io.discloader.discloader.util.DLUtil;
import io.discloader.discloader.util.DLUtil.ChannelType;

/**
 * @author Perry Berman
 */
public class MessageCreate extends AbstractHandler {

	public MessageCreate(DiscSocket socket) {
		super(socket);
	}

	@Override
	public void handle(SocketPacket packet) {
		MessageJSON data = this.gson.fromJson(gson.toJson(packet.d), MessageJSON.class);
		try {
			ITextChannel channel = this.socket.loader.textChannels.get(data.channel_id);
			if (channel == null) channel = this.socket.loader.privateChannels.get(data.channel_id);
			IMessage message = new Message(channel, data);
			channel.getMessages().put(message.getID(), message);
			if (channel.isTyping(message.getAuthor())) {
				channel.getTyping().remove(message.getAuthor().getID());
			}
			MessageCreateEvent event = new MessageCreateEvent(message);
			loader.emit(DLUtil.Events.MESSAGE_CREATE, event);
			loader.emit(event);
			if (channel.getType() == ChannelType.TEXT) {
				GuildMessageCreateEvent event2 = new GuildMessageCreateEvent(message);
				loader.emit("GuildMessageCreate", event2);
				loader.emit(event2);
			} else if (channel.getType() == ChannelType.DM) {
				PrivateMessageCreateEvent event2 = new PrivateMessageCreateEvent(message);
				loader.emit(DLUtil.Events.PRIVATE_MESSAGE_CREATE, event2);
				loader.emit(event2);
			}
			CommandHandler.handleMessageCreate(event);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
