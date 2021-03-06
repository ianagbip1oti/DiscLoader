/**
 * 
 */
package io.discloader.discloader.network.gateway.packets;

import io.discloader.discloader.common.event.IEventListener;
import io.discloader.discloader.common.event.channel.ChannelUpdateEvent;
import io.discloader.discloader.entity.channel.IChannel;
import io.discloader.discloader.entity.channel.ITextChannel;
import io.discloader.discloader.entity.guild.IGuild;
import io.discloader.discloader.entity.message.IMessage;
import io.discloader.discloader.network.gateway.DiscSocket;
import io.discloader.discloader.network.json.ChannelJSON;
import io.discloader.discloader.util.DLUtil.Events;

/**
 * @author Perry Berman
 */
public class ChannelUpdate extends AbstractHandler {

	public ChannelUpdate(DiscSocket socket) {
		super(socket);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void handle(SocketPacket packet) {
		String d = gson.toJson(packet.d);
		ChannelJSON data = gson.fromJson(d, ChannelJSON.class);
		IGuild guild = null;
		IChannel oldChannel = loader.channels.get(data.id);
		IChannel channel = null;
		if (data.guild_id != null) {
			guild = loader.guilds.get(data.guild_id);
			channel = loader.addChannel(data, guild);
		} else {
			channel = loader.addChannel(data);
		}
		if (oldChannel instanceof ITextChannel) {
			ITextChannel oitc = (ITextChannel) oldChannel, itc = (ITextChannel) channel;
			for (IMessage message : oitc.getMessages().values()) {
				itc.getMessages().put(message.getID(), message);
			}
		}
		ChannelUpdateEvent event = new ChannelUpdateEvent(channel, oldChannel);
		loader.emit(Events.CHANNEL_UPDATE, event);
		for (IEventListener e : loader.handlers) {
			e.ChannelUpdate(event);
		}
	}

}
