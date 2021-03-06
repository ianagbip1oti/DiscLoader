package io.discloader.discloader.core.entity.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.discloader.discloader.core.entity.Overwrite;
import io.discloader.discloader.core.entity.Permission;
import io.discloader.discloader.core.entity.guild.Guild;
import io.discloader.discloader.core.entity.guild.GuildMember;
import io.discloader.discloader.core.entity.guild.Role;
import io.discloader.discloader.entity.IOverwrite;
import io.discloader.discloader.entity.IPermission;
import io.discloader.discloader.entity.Permissions;
import io.discloader.discloader.entity.channel.IGuildChannel;
import io.discloader.discloader.entity.guild.IGuild;
import io.discloader.discloader.entity.guild.IGuildMember;
import io.discloader.discloader.entity.guild.IRole;
import io.discloader.discloader.network.json.ChannelJSON;
import io.discloader.discloader.network.rest.actions.channel.SetOverwrite;
import io.discloader.discloader.network.rest.actions.channel.close.CloseGuildChannel;

/**
 * Represents any channel in a guild
 * 
 * @author Perry Berman
 */
public class GuildChannel extends Channel implements IGuildChannel {
	
	/**
	 * The {@link Guild} the channel belongs to. <br>
	 * This property <u>must</u> be {@code null} if the {@link #type} of the channel is {@code "dm"}, or {@code "groupDM"}.
	 * 
	 * @author Perry Berman
	 * @since 0.0.1
	 */
	protected final IGuild guild;
	
	/**
	 * The channel's name
	 */
	public String name;
	
	/**
	 * The channel's position
	 */
	public int position;
	
	/**
	 * A {@link HashMap} of the channel's {@link Overwrite overwrites}. Indexed by {@link Overwrite#id}.
	 * 
	 * @author Perry Berman
	 * @since 0.0.1
	 */
	private HashMap<String, IOverwrite> overwrites;
	
	public GuildChannel(IGuild guild, ChannelJSON channel) {
		super(guild.getLoader(), channel);
		
		this.guild = guild;
		
		overwrites = new HashMap<>();
	}
	
	@Override
	public CompletableFuture<IGuildChannel> clone() {
		return guild.createChannel(name, type.name());
	}
	
	@Override
	public CompletableFuture<IGuildChannel> delete() {
		return new CloseGuildChannel(this).execute();
	}
	
	/**
	 * Changes the channels settings
	 * 
	 * @param name The new name for the channel
	 * @param topic The new topic for the channel
	 * @param position The new position for the channel
	 * @param bitrate The new bitrate
	 * @param userLimit The new userLimit
	 * @return A Future that completes with an IGuildChannel if successful
	 */
	@Override
	public CompletableFuture<IGuildChannel> edit(String name, String topic, int position, int bitrate, int userLimit) {
		CompletableFuture<IGuildChannel> future = new CompletableFuture<>();
		loader.rest.modifyGuildChannel(this, name, topic, position, bitrate, userLimit).thenAcceptAsync(channel -> {
			future.complete(channel);
		});
		return future;
	}
	
	@Override
	public HashMap<String, IGuildMember> getMembers() {
		HashMap<String, IGuildMember> members = new HashMap<>();
		for (IGuildMember member : guild.getMembers().values()) {
			if (this.permissionsFor(member).hasPermission(Permissions.READ_MESSAGES, false))
				members.put(member.getID(), member);
		}
		return members;
	}
	
	public HashMap<String, IOverwrite> getOverwrites() {
		return overwrites;
	}
	
	@Override
	public boolean isPrivate() {
		return false;
	}
	
	@Override
	public IOverwrite overwriteFor(Role role) {
		return overwrites.get(role.id);
	}
	
	@Override
	public Map<String, IOverwrite> overwritesOf(IGuildMember member) {
		HashMap<String, IOverwrite> Overwrites = new HashMap<>();
		for (IRole role : member.getRoles().values()) {
			if (overwrites.get(role.getID()) != null)
				Overwrites.put(role.getID(), overwrites.get(role.getID()));
		}
		if (overwrites.get(member.getID()) != null)
			Overwrites.put(member.getID(), overwrites.get(member.getID()));
		return Overwrites;
	}
	
	@Override
	public IPermission permissionsFor(IGuildMember member) {
		int raw = 0;
		if (guild.isOwner(member))
			return new Permission(member, this, 2146958463);
		for (IRole role : member.getRoles().values())
			raw |= role.getPermissions().toInt();
		for (IOverwrite overwrite : this.overwritesOf(member).values()) {
			raw |= overwrite.getAllowed();
			raw &= ~overwrite.getDenied();
		}
		return new Permission(member, this, raw);
	}
	
	@Override
	public CompletableFuture<IGuildChannel> setName(String name) {
		return edit(name, null, position, 64000, 0);
	}
	
	@Override
	public CompletableFuture<IOverwrite> setPermissions(int allow, int deny, GuildMember member) {
		return new SetOverwrite(this, new Overwrite(allow, deny, member)).execute();
	}
	
	@Override
	public CompletableFuture<IOverwrite> setPermissions(int allow, int deny, Role role) {
		return new SetOverwrite(this, new Overwrite(allow, deny, role)).execute();
	}
	
	@Override
	public CompletableFuture<IGuildChannel> setPosition(int position) {
		return edit(name, null, position, 64000, 0);
	}
	
	@Override
	public void setup(ChannelJSON data) {
		super.setup(data);
		name = data.name;
		position = data.position;
	}
	
	/*
	 * (non-Javadoc)
	 * @see io.discloader.discloader.entity.channel.IGuildChannel#getGuild()
	 */
	@Override
	public IGuild getGuild() {
		return null;
	}
	
}
