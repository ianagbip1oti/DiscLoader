package io.discloader.discloader.network.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import com.google.gson.Gson;

import io.discloader.discloader.common.DiscLoader;
import io.discloader.discloader.core.entity.RichEmbed;
import io.discloader.discloader.core.entity.channel.GuildChannel;
import io.discloader.discloader.core.entity.channel.TextChannel;
import io.discloader.discloader.core.entity.channel.VoiceChannel;
import io.discloader.discloader.core.entity.guild.Emoji;
import io.discloader.discloader.core.entity.guild.Guild;
import io.discloader.discloader.core.entity.guild.GuildMember;
import io.discloader.discloader.core.entity.guild.Role;
import io.discloader.discloader.core.entity.message.Message;
import io.discloader.discloader.core.entity.user.DLUser;
import io.discloader.discloader.core.entity.user.OAuth2Application;
import io.discloader.discloader.core.entity.user.User;
import io.discloader.discloader.entity.channel.ITextChannel;
import io.discloader.discloader.entity.guild.IGuild;
import io.discloader.discloader.entity.guild.IGuildEmoji;
import io.discloader.discloader.entity.guild.IGuildMember;
import io.discloader.discloader.entity.guild.IRole;
import io.discloader.discloader.entity.message.IMessage;
import io.discloader.discloader.entity.sendable.Attachment;
import io.discloader.discloader.entity.sendable.CreateEmoji;
import io.discloader.discloader.entity.sendable.CreateRole;
import io.discloader.discloader.entity.sendable.EditChannel;
import io.discloader.discloader.entity.sendable.FetchMembers;
import io.discloader.discloader.entity.sendable.SendableMessage;
import io.discloader.discloader.entity.user.IUser;
import io.discloader.discloader.network.json.ChannelJSON;
import io.discloader.discloader.network.json.GuildJSON;
import io.discloader.discloader.network.json.InviteJSON;
import io.discloader.discloader.network.json.MemberJSON;
import io.discloader.discloader.network.json.MessageJSON;
import io.discloader.discloader.network.json.OAuthApplicationJSON;
import io.discloader.discloader.network.json.RoleJSON;
import io.discloader.discloader.network.json.UserJSON;
import io.discloader.discloader.util.DLUtil;
import io.discloader.discloader.util.DLUtil.Endpoints;
import io.discloader.discloader.util.DLUtil.Methods;

public class RESTManager {
	
	public Gson gson;
	public HashMap<String, RESTQueue> queues;
	public DiscLoader loader;
	
	// private Map<String, Route> routes;
	
	public RESTManager(DiscLoader loader) {
		this.loader = loader;
		gson = new Gson();
		// routes = new HashMap<>();
		queues = new HashMap<>();
	}
	
	public CompletableFuture<IGuildMember> banMember(Guild guild, IGuildMember member) {
		CompletableFuture<IGuildMember> future = new CompletableFuture<>();
		this.makeRequest(Endpoints.guildBanMember(member.getGuild().getID(), member.getID()), Methods.PUT, true).thenAcceptAsync(action -> {
			future.complete(member);
		});
		return future;
	}
	
	public CompletableFuture<Integer> beginPrune(Guild guild, int days) {
		CompletableFuture<Integer> future = new CompletableFuture<>();
		JSONObject payload = new JSONObject().put("days", days);
		this.makeRequest(Endpoints.guildPrune(guild.getID()), Methods.POST, true, payload).thenAcceptAsync(action -> {
			future.complete(Integer.parseInt(action, 10));
		});
		return future;
	}
	
	public CompletableFuture<Emoji> createEmoji(Guild guild, String name, String image) {
		CompletableFuture<Emoji> future = new CompletableFuture<>();
		CreateEmoji ce = new CreateEmoji(name, image);
		this.makeRequest(Endpoints.guildEmojis(guild.getID()), Methods.POST, true, ce).thenAcceptAsync(action -> {
			System.out.println(action);
		});
		return future;
	}
	
	public CompletableFuture<TextChannel> createTextChannel(Guild guild, JSONObject data) {
		CompletableFuture<TextChannel> future = new CompletableFuture<TextChannel>();
		this.makeRequest(DLUtil.Endpoints.guildChannels(guild.getID()), DLUtil.Methods.POST, true, data.put("type", "text")).thenAcceptAsync(action -> {
			future.complete((TextChannel) this.loader.addChannel(this.gson.fromJson(action, ChannelJSON.class), guild));
		});
		return future;
	}
	
	public CompletableFuture<VoiceChannel> createVoiceChannel(Guild guild, JSONObject data) {
		CompletableFuture<VoiceChannel> future = new CompletableFuture<VoiceChannel>();
		this.makeRequest(DLUtil.Endpoints.guildChannels(guild.getID()), DLUtil.Methods.POST, true, data.put("type", "voice")).thenAcceptAsync(action -> {
			future.complete((VoiceChannel) this.loader.addChannel(this.gson.fromJson(action, ChannelJSON.class), guild));
		});
		return future;
	}
	
	public CompletableFuture<IGuildEmoji> deleteEmoji(Emoji emoji) {
		CompletableFuture<IGuildEmoji> future = new CompletableFuture<>();
		this.makeRequest(Endpoints.guildEmoji(emoji.getGuild().getID(), emoji.getID()), Methods.DELETE, true).thenAcceptAsync(action -> {
			future.complete(emoji);
		});
		return future;
	}
	
	public CompletableFuture<IMessage> deleteMessage(ITextChannel channel, IMessage message) {
		CompletableFuture<IMessage> future = new CompletableFuture<>();
		this.makeRequest(DLUtil.Endpoints.message(channel.getID(), message.getID()), DLUtil.Methods.DELETE, true).thenAcceptAsync(action -> {
			future.complete(message);
		});
		return future;
	}
	
	public CompletableFuture<OAuth2Application> getApplicationInfo() {
		CompletableFuture<OAuth2Application> future = new CompletableFuture<OAuth2Application>();
		this.makeRequest(DLUtil.Endpoints.currentOAuthApplication, DLUtil.Methods.GET, true).thenAcceptAsync(data -> {
			OAuthApplicationJSON appData = this.gson.fromJson(data, OAuthApplicationJSON.class);
			IUser owner = this.loader.addUser(appData.owner);
			future.complete(new OAuth2Application(appData, owner));
		});
		
		return future;
	}
	
	public CompletableFuture<GuildMember> giveRole(GuildMember member, IRole role) {
		CompletableFuture<GuildMember> future = new CompletableFuture<GuildMember>();
		this.makeRequest(Endpoints.guildMemberRole(member.guild.getID(), member.getID(), role.getID()), Methods.PUT, true).thenAcceptAsync(action -> {
			future.complete(member);
		});
		return future;
	}
	
	public void handleQueue(String route) {
		this.queues.get(route).handle();
	}
	
	public CompletableFuture<GuildMember> kickMember(GuildMember member) {
		CompletableFuture<GuildMember> future = new CompletableFuture<>();
		this.makeRequest(Endpoints.guildMember(member.guild.getID(), member.getID()), Methods.DELETE, true).thenAcceptAsync(action -> {
			future.complete(member);
		});
		return future;
	}
	
	public CompletableFuture<IGuildMember> loadGuildMember(IGuild guild, String memberID) {
		CompletableFuture<IGuildMember> future = new CompletableFuture<>();
		this.makeRequest(DLUtil.Endpoints.guildMember(guild.getID(), memberID), DLUtil.Methods.GET, true).thenAcceptAsync(action -> {
			future.complete(guild.addMember(this.gson.fromJson(action, MemberJSON.class)));
		});
		return future;
	}
	
	public CompletableFuture<HashMap<String, IGuildMember>> loadGuildMembers(IGuild guild, int limit, String after) {
		CompletableFuture<HashMap<String, IGuildMember>> future = new CompletableFuture<>();
		FetchMembers fetchMem = new FetchMembers(limit, after);
		this.makeRequest(Endpoints.guildMembers(guild.getID()), Methods.GET, true, fetchMem).thenAcceptAsync(action -> {
			HashMap<String, IGuildMember> members = new HashMap<>();
			MemberJSON[] data = DLUtil.gson.fromJson(action, MemberJSON[].class);
			for (MemberJSON mem : data) {
				members.put(mem.user.id, guild.addMember(mem));
			}
			future.complete(members);
		});
		return future;
	}
	
	public CompletableFuture<String> makeRequest(String url, int method, boolean auth) {
		return this.makeRequest(url, method, auth, null);
	}
	
	public CompletableFuture<String> makeRequest(String url, int method, boolean auth, Object data) {
		APIRequest request = new APIRequest(url, method, auth, data);
		CompletableFuture<String> future = new CompletableFuture<String>();
		if (!this.queues.containsKey(url)) {
			this.queues.put(url, new RESTQueue(this));
		}
		
		request.setFuture(future);
		this.queues.get(url).addToQueue(request);
		this.handleQueue(url);
		return future;
	}
	
	public CompletableFuture<Guild> modifyGuild(Guild guild, JSONObject data) {
		CompletableFuture<Guild> future = new CompletableFuture<Guild>();
		this.makeRequest(DLUtil.Endpoints.guild(guild.getID()), DLUtil.Methods.PATCH, true, data).thenAcceptAsync(action -> {
			guild.setup(this.gson.fromJson(action, GuildJSON.class));
			future.complete(guild);
		});
		return future;
	}
	
	public CompletableFuture<Integer> pruneCount(Guild guild, int days) {
		CompletableFuture<Integer> future = new CompletableFuture<>();
		JSONObject payload = new JSONObject().put("days", days);
		this.makeRequest(Endpoints.guildPrune(guild.getID()), Methods.GET, true, payload).thenAcceptAsync(action -> {
			future.complete(Integer.parseInt(action, 10));
		});
		return future;
	}
	
	public CompletableFuture<IGuildMember> removeMember(IGuild guild, IGuildMember member) {
		CompletableFuture<IGuildMember> future = new CompletableFuture<>();
		this.makeRequest(Endpoints.guildMember(member.getGuild().getID(), member.getID()), Methods.DELETE, true).thenAcceptAsync(action -> {
			future.complete(member);
		});
		return future;
	}
	
	public CompletableFuture<Message> sendMessage(ITextChannel channel, String content, RichEmbed embed, Attachment attachment, File file) {
		if (content != null && content.length() < 0 && (embed == null && attachment == null))
			return null;
		CompletableFuture<Message> msgSent = new CompletableFuture<Message>();
		this.makeRequest(DLUtil.Endpoints.messages(channel.getID()), DLUtil.Methods.POST, true, new SendableMessage(content, embed, attachment, file)).thenAcceptAsync(action -> {
			msgSent.complete(new Message(channel, this.gson.fromJson(action, MessageJSON.class)));
		});
		return msgSent;
	}
	
	public CompletableFuture<User> setAvatar(String avatar) {
		CompletableFuture<User> future = new CompletableFuture<User>();
		try {
			String base64 = new String("data:image/jpg;base64," + Base64.encodeBase64String(Files.readAllBytes(Paths.get(avatar))));
			this.makeRequest(DLUtil.Endpoints.currentUser, DLUtil.Methods.PATCH, true, new JSONObject().put("avatar", base64)).thenAcceptAsync(action -> {
				loader.user.setup(this.gson.fromJson(action, UserJSON.class));
				future.complete(loader.user);
			});
		} catch (IOException e) {
			future.completeExceptionally(e);
		}
		return future;
	}
	
	public CompletableFuture<IGuildMember> setNick(IGuildMember member, String nick) {
		CompletableFuture<IGuildMember> future = new CompletableFuture<>();
		String endpoint = member.getID().equals(loader.user.getID()) ? DLUtil.Endpoints.guildNick(member.getGuild().getID()) : DLUtil.Endpoints.guildMember(member.getGuild().getID(), member.getID());
		makeRequest(endpoint, DLUtil.Methods.PATCH, true, new JSONObject().put("nick", nick)).thenAcceptAsync(action -> {
			future.complete(member);
		});
		return future;
	}
	
	public CompletableFuture<DLUser> setUsername(String username) {
		CompletableFuture<DLUser> future = new CompletableFuture<>();
		makeRequest(DLUtil.Endpoints.currentUser, DLUtil.Methods.PATCH, true, new JSONObject().put("username", username)).thenAcceptAsync(action -> {
			loader.user.setup(this.gson.fromJson(action, UserJSON.class));
			future.complete(loader.user);
		});
		return future;
	}
	
	public CompletableFuture<IGuildMember> takeRole(GuildMember member, IRole role) {
		CompletableFuture<IGuildMember> future = new CompletableFuture<>();
		this.makeRequest(Endpoints.guildMemberRole(member.guild.getID(), member.getID(), role.getID()), Methods.DELETE, true).thenAcceptAsync(action -> {
			future.complete(member);
		});
		return future;
	}
	
	public CompletableFuture<Role> createRole(Guild guild, String name, int permissions, int color, boolean hoist, boolean mentionable) {
		CompletableFuture<Role> future = new CompletableFuture<Role>();
		CreateRole payload = new CreateRole(name, permissions, color, hoist, mentionable);
		this.makeRequest(Endpoints.guildRoles(guild.getID()), Methods.POST, true, payload).thenAcceptAsync(action -> {
			RoleJSON data = gson.fromJson(action, RoleJSON.class);
			future.complete(guild.addRole(data));
		});
		return future;
	}
	
	public CompletableFuture<InviteJSON[]> getInvites(Guild guild) {
		CompletableFuture<InviteJSON[]> future = new CompletableFuture<>();
		this.makeRequest(Endpoints.guildInvites(guild.getID()), Methods.GET, true).thenAcceptAsync(action -> {
			InviteJSON[] data = gson.fromJson(action, InviteJSON[].class);
			future.complete(data);
		});
		
		return future;
	}
	
	public CompletableFuture<GuildChannel> modifyGuildChannel(GuildChannel channel, String name, String topic, int position, int bitrate, int userLimit) {
		CompletableFuture<GuildChannel> future = new CompletableFuture<>();
		EditChannel d = new EditChannel(name, topic, position, bitrate, userLimit);
		this.makeRequest(Endpoints.channel(channel.getID()), Methods.PATCH, true, d).thenAcceptAsync(action -> {
			ChannelJSON cd = gson.fromJson(action, ChannelJSON.class);
			channel.setup(cd);
			future.complete(channel);
		});
		
		return future;
	}
	
}
