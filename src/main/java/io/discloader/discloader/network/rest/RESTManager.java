package io.discloader.discloader.network.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import com.google.gson.Gson;

import io.discloader.discloader.common.DiscLoader;
import io.discloader.discloader.common.structures.Guild;
import io.discloader.discloader.common.structures.GuildMember;
import io.discloader.discloader.common.structures.Message;
import io.discloader.discloader.common.structures.RichEmbed;
import io.discloader.discloader.common.structures.User;
import io.discloader.discloader.common.structures.channels.TextChannel;
import io.discloader.discloader.network.gateway.json.GuildJSON;
import io.discloader.discloader.network.gateway.json.MemberJSON;
import io.discloader.discloader.network.gateway.json.MessageJSON;
import io.discloader.discloader.network.gateway.json.UserJSON;
import io.discloader.discloader.util.Constant;

public class RESTManager {
	public Gson gson;
	public HashMap<String, RESTQueue> queues;
	public DiscLoader loader;

	public RESTManager(DiscLoader loader) {
		this.loader = loader;

		this.gson = new Gson();

		this.queues = new HashMap<String, RESTQueue>();
	}

	public void handleQueue(String route) {
		this.queues.get(route).handle();
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

	public CompletableFuture<String> makeRequest(String url, int method, boolean auth) {
		return this.makeRequest(url, method, auth, null);
	}

	public CompletableFuture<Message> sendMessage(TextChannel channel, String content, RichEmbed embed) {
		if (content.length() < 1)
			return null;
		CompletableFuture<Message> msgSent = new CompletableFuture<Message>();
		this.makeRequest(Constant.Endpoints.messages(channel.id), Constant.Methods.POST, true,
				new JSONObject().put("content", content).toString()).thenAcceptAsync(action -> {
					msgSent.complete(new Message(channel, this.gson.fromJson(action, MessageJSON.class)));
				});
		return msgSent;
	}

	public CompletableFuture<Message> editMessage(TextChannel channel, Message message, String content, RichEmbed embed) {
		if (content.length() < 1)
			return null;
		CompletableFuture<Message> future = new CompletableFuture<Message>();
		this.makeRequest(Constant.Endpoints.message(channel.id, message.id), Constant.Methods.PATCH, true,
				new JSONObject().put("content", content).put("embed", embed).toString()).thenAcceptAsync(action -> {
					future.complete(new Message(channel, this.gson.fromJson(action, MessageJSON.class)));
				});
		return future;
	}

	public CompletableFuture<Message> deleteMessage(TextChannel channel, Message message) {
		CompletableFuture<Message> future = new CompletableFuture<Message>();
		this.makeRequest(Constant.Endpoints.message(channel.id, message.id), Constant.Methods.DELETE, true)
				.thenAcceptAsync(action -> {
					future.complete(message);
				});
		return future;
	}

	public CompletableFuture<User> setUsername(String username) {
		CompletableFuture<User> future = new CompletableFuture<User>();
		this.makeRequest(Constant.Endpoints.currentUser, Constant.Methods.PATCH, true,
				new JSONObject().put("username", username)).thenAcceptAsync(action -> {
					future.complete(this.loader.user.patch(this.gson.fromJson(action, UserJSON.class)));
				});
		return future;
	}

	public CompletableFuture<User> setAvatar(String avatar) {
		CompletableFuture<User> future = new CompletableFuture<User>();
		try {
			String base64 = new String(
					"data:image/jpg;base64," + Base64.encodeBase64String(Files.readAllBytes(Paths.get(avatar))));
			this.makeRequest(Constant.Endpoints.currentUser, Constant.Methods.PATCH, true,
					new JSONObject().put("avatar", base64)).thenAcceptAsync(action -> {
						future.complete(this.loader.user.patch(this.gson.fromJson(action, UserJSON.class)));
					});
		} catch (IOException e) {
			future.completeExceptionally(e);
		}
		return future;
	}

	public CompletableFuture<GuildMember> setNick(GuildMember member, String nick) {
		CompletableFuture<GuildMember> future = new CompletableFuture<GuildMember>();
		String endpoint = member.id == this.loader.user.id ? Constant.Endpoints.guildNick(member.guild.id)
				: Constant.Endpoints.guildMember(member.guild.id, member.id);
		this.makeRequest(endpoint, Constant.Methods.PATCH, true, new JSONObject().put("nick", nick))
				.thenAcceptAsync(action -> {
					member.nick = nick;
					future.complete(member);
				});
		return future;
	}

	public CompletableFuture<Guild> modifyGuild(Guild guild, JSONObject data) {
		CompletableFuture<Guild> future = new CompletableFuture<Guild>();
		this.makeRequest(Constant.Endpoints.guild(guild.id), Constant.Methods.PATCH, true, data)
				.thenAcceptAsync(action -> {
					guild.setup(this.gson.fromJson(action, GuildJSON.class));
					future.complete(guild);
				});
		return future;
	}

	public CompletableFuture<GuildMember> loadGuildMember(Guild guild, String memberID) {
		CompletableFuture<GuildMember> future = new CompletableFuture<GuildMember>();
		this.makeRequest(Constant.Endpoints.guildMember(guild.id, memberID), Constant.Methods.GET, true)
				.thenAcceptAsync(action -> {
					future.complete(guild.addMember(this.gson.fromJson(action, MemberJSON.class)));
				});
		return future;
	}

}