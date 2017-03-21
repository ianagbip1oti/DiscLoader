/**
 * 
 */
package io.discloader.discloader.network.gateway.packets;

import io.discloader.discloader.common.DiscLoader;
import io.discloader.discloader.common.event.ChannelUpdateEvent;
import io.discloader.discloader.common.event.IEventListener;
import io.discloader.discloader.entity.channels.Channel;
import io.discloader.discloader.entity.guild.Guild;
import io.discloader.discloader.network.gateway.DiscSocket;
import io.discloader.discloader.network.json.ChannelJSON;
import io.discloader.discloader.util.DLUtil.Events;

/**
 * @author Perry Berman
 *
 */
public class ChannelUpdate extends DLPacket {

	public ChannelUpdate(DiscSocket socket) {
		super(socket);
	}
	
	public void handle(SocketPacket packet) {
		String d = gson.toJson(packet.d);
		ChannelJSON data = gson.fromJson(d, ChannelJSON.class);
		Guild guild = null;
		Channel oldChannel = loader.channels.get(data.id);
		Channel channel = null;
		if (data.guild_id != null) {
			guild = loader.guilds.get(data.guild_id);
			channel = loader.addChannel(data, guild);
		} else {
			channel = loader.addChannel(data);
		}
		if (oldChannel instanceof ITextChannel) {
			ITextChannel oitc = (ItextChannel) oldChannel, itc = (ITextChannel) channel;
			for (Message message : oldChannel.getMessages().values()) {
				itc.getMessages().put(message.id, message);
			}
		}
		ChannelUpdateEvent event = new ChannelUpdateEvent(channel, oldChannel);
		loader.emit(Events.CHANNEL_UPDATE, event);
		for (IEventListener e : DiscLoader.handlers.values()) {
			e.ChannelUpdate(event);
		}
	}


}
