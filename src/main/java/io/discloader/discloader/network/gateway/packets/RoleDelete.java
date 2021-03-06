package io.discloader.discloader.network.gateway.packets;

import io.discloader.discloader.common.event.IEventListener;
import io.discloader.discloader.common.event.guild.role.GuildRoleDeleteEvent;
import io.discloader.discloader.entity.guild.IGuild;
import io.discloader.discloader.entity.guild.IRole;
import io.discloader.discloader.network.gateway.DiscSocket;
import io.discloader.discloader.network.json.GuildRoleJSON;
import io.discloader.discloader.util.DLUtil;

/**
 * @author Perry Berman
 *
 */
public class RoleDelete extends AbstractHandler {

	public RoleDelete(DiscSocket socket) {
		super(socket);
	}
	
	@Override
	public void handle(SocketPacket packet) {
		String d = this.gson.toJson(packet.d);
		GuildRoleJSON data = this.gson.fromJson(d, GuildRoleJSON.class);
		IGuild guild = this.loader.guilds.get(data.guild_id);
		IRole role = guild.getRoles().remove(data.role_id);
		GuildRoleDeleteEvent event = new GuildRoleDeleteEvent(role);
		this.loader.emit(DLUtil.Events.GUILD_ROLE_DELETE, event);
		for (IEventListener e : loader.handlers) {
			e.GuildRoleDelete(event);
		}
	}


}
