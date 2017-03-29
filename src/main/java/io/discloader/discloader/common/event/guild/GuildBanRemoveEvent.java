/**
 * 
 */
package io.discloader.discloader.common.event.guild;

import io.discloader.discloader.common.entity.guild.Guild;
import io.discloader.discloader.common.entity.user.User;
import io.discloader.discloader.common.event.DLEvent;

/**
 * @author Perry Berman
 *
 */
public class GuildBanRemoveEvent extends DLEvent {

	public final Guild guild;
	public final User user;
	

	/**
	 * Creates a new GuildBanRemoveEvent Object
	 * @param guild The guild the user was unbanned from
	 * @param user The use that has been unbanned
	 */
	public GuildBanRemoveEvent(Guild guild, User user) {
		super(guild.getLoader());
		this.guild = guild;
		this.user = user;
	}

}
