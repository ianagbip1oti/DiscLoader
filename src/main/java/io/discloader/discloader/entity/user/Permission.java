package io.discloader.discloader.entity.user;

import io.discloader.discloader.entity.channels.Channel;
import io.discloader.discloader.entity.guild.GuildMember;
import io.discloader.discloader.util.DLUtil;

/**
 * This object contains the {@link #member}'s evaulated permissions integer for
 * the {@link #channel}.
 * 
 * @author Perry Berman
 * @since 0.0.1
 * @see DLUtil.PermissionFlags
 */
public class Permission {

	/**
	 * The {@link GuildMember} who's permissions were evaluated in the specified
	 * {@link #channel}
	 */
	public final GuildMember member;

	/**
	 * The channel in which the {@link #member}'s permissions were evaluated.
	 */
	public final Channel channel;

	/**
	 * The raw 53bit permissions {@link Integer}
	 */
	public final int raw;

	public Permission(GuildMember member, Channel channel, int raw) {
		this.member = member;
		this.channel = channel;
		this.raw = raw;
	}

	/**
	 * Checks if the member has the specified permission.
	 * 
	 * @param permission A {@link DLUtil.PermissionFlags Permission Flag}
	 * @return {@code true}, if the user has the specified permission. Otherwise
	 *         {@code false}.
	 */
	public boolean hasPermission(int permission) {
		return this.hasPermission(permission, false);
	}

	/**
	 * Checks if the member has the specified permission. <br>
	 * {@code boolean sendMessages = channel.permissionsFor(member).hasPermission(PermissionFlags.SEND_MESSAGES);}
	 * 
	 * @param permission A {@link DLUtil.PermissionFlags Permission Flag}
	 * @param explicit Whether or not the member explicitly has the permission
	 * @return {@code true}, if the user has the specified permission. Otherwise
	 *         {@code false}.
	 */
	public boolean hasPermission(int permission, boolean explicit) {
		if (!explicit && (this.raw & DLUtil.PermissionFlags.ADMINISTRATOR) > 0)
			return true;
		return (this.raw & permission) > 0;
	}
}