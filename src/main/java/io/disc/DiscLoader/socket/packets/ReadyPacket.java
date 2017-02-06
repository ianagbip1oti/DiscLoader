package io.disc.DiscLoader.socket.packets;

import com.google.gson.Gson;

import io.disc.DiscLoader.objects.gateway.ChannelJSON;
import io.disc.DiscLoader.objects.gateway.GuildJSON;
import io.disc.DiscLoader.objects.gateway.Ready;
import io.disc.DiscLoader.socket.DiscSocket;

public class ReadyPacket extends DiscPacket {
	public ReadyPacket(DiscSocket socket) {
		super(socket);
	}

	public void handle(SocketPacket packet) {
		Gson gson = new Gson();
		String d = gson.toJson(packet.d);
		Ready ready = gson.fromJson(d, Ready.class);

		// send first heartbeat in response to ready packet
		this.socket.sendHeartbeat(false);

		// setup the Loaders user object
		this.socket.loader.user = this.socket.loader.addUser(ready.user);

		// System.out.println(ready.v);
		GuildJSON[] guilds = ready.guilds;
		for (int i = 0; i < guilds.length; i++) {
			this.socket.loader.addGuild(guilds[i]);
		}

		for (ChannelJSON data : ready.private_channels)
			this.socket.loader.addChannel(data);

		this.socket.sessionID = ready.session_id;
		// check if the loader is ready to rock & roll
		this.socket.loader.checkReady();
	}
}
