package io.discloader.discloader.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;

import io.discloader.discloader.client.command.CommandHandler;
import io.discloader.discloader.client.logger.DLLogger;
import io.discloader.discloader.client.logger.ProgressLogger;
import io.discloader.discloader.client.registry.ClientRegistry;
import io.discloader.discloader.client.render.WindowFrame;
import io.discloader.discloader.common.discovery.ModCandidate;
import io.discloader.discloader.common.discovery.ModContainer;
import io.discloader.discloader.common.discovery.ModDiscoverer;
import io.discloader.discloader.common.event.IEventListener;
import io.discloader.discloader.common.registry.ModRegistry;
import io.discloader.discloader.common.start.Main;
import io.discloader.discloader.entity.DLUser;
import io.discloader.discloader.entity.Guild;
import io.discloader.discloader.entity.User;
import io.discloader.discloader.entity.channels.Channel;
import io.discloader.discloader.entity.channels.GroupChannel;
import io.discloader.discloader.entity.channels.PrivateChannel;
import io.discloader.discloader.entity.channels.TextChannel;
import io.discloader.discloader.entity.channels.VoiceChannel;
import io.discloader.discloader.entity.voice.VoiceConnection;
import io.discloader.discloader.network.gateway.DiscSocket;
import io.discloader.discloader.network.json.ChannelJSON;
import io.discloader.discloader.network.json.GuildJSON;
import io.discloader.discloader.network.json.UserJSON;
import io.discloader.discloader.network.rest.RESTManager;
import io.discloader.discloader.util.Constants;

/**
 * @author Perry Berman, Zachary Waldron
 */
public class DiscLoader {

	public static final Logger LOG = new DLLogger(DiscLoader.class.getName()).getLogger();
	
	public DiscSocket socket;

	public String token;

	public boolean ready;

	public final ClientRegistry clientRegistry;

	public RESTManager rest;

	public final AudioPlayerManager playerManager;

	public static final HashMap<String, IEventListener> handlers = new HashMap<String, IEventListener>();

	public final int shards;
	public final int shard;

	/**
	 * A HashMap of the client's cached users. Indexed by {@link User#id}.
	 * 
	 * @author Perry Berman
	 * @see User
	 * @see HashMap
	 */
	public HashMap<String, User> users;

	/**
	 * A HashMap of the client's cached channels. Indexed by {@link Channel#id}.
	 * 
	 * @author Perry Berman
	 * @see Channel
	 * @see HashMap
	 */
	public HashMap<String, Channel> channels;
	
	public HashMap<String, GroupChannel> groupChannels;

	/**
	 * A HashMap of the client's cached PrivateChannels. Indexed by
	 * {@link Channel#id}.
	 * 
	 * @see Channel
	 * @see PrivateChannel
	 * @see HashMap
	 * @author Perry Berman
	 */
	public HashMap<String, PrivateChannel> privateChannels;

	/**
	 * A HashMap of the client's cached TextChannels. Indexed by
	 * {@link Channel#id}.
	 * 
	 * @see Channel
	 * @see TextChannel
	 * @see HashMap
	 * @author Perry Berman
	 */
	public HashMap<String, TextChannel> textChannels;

	/**
	 * A HashMap of the client's cached VoiceChannels. Indexed by
	 * {@link Channel#id}.
	 * 
	 * @see Channel
	 * @see VoiceChannel
	 * @see HashMap
	 * @author Perry Berman
	 */
	public HashMap<String, VoiceChannel> voiceChannels;
	
	/**
	 * A HashMap of the client's voice connections. Indexed by {@link Guild#id}.
	 * @author Perry Berman
	 * @since 0.0.3
	 */
	public HashMap<String, VoiceConnection> voiceConnections;

	/**
	 * A HashMap of the client's cached Guilds. Indexed by {@link Guild#id}
	 * 
	 * @see Guild
	 * @see HashMap
	 * @author Perry Berman
	 */
	public HashMap<String, Guild> guilds;

	/**
	 * The User we are currently logged in as.
	 */
	public DLUser user;

	public Timer timer;

	public DiscLoader(int shards, int shard) {

		this.shards = shards;

		this.shard = shard;

		this.socket = new DiscSocket(this);

		this.rest = new RESTManager(this);

		this.clientRegistry = new ClientRegistry();

		this.users = new HashMap<String, User>();

		this.channels = new HashMap<String, Channel>();
		
		this.groupChannels = new HashMap<String, GroupChannel>();

		this.privateChannels = new HashMap<String, PrivateChannel>();

		this.textChannels = new HashMap<String, TextChannel>();

		this.voiceChannels = new HashMap<String, VoiceChannel>();
		
		this.voiceConnections = new HashMap<String, VoiceConnection>();

		this.guilds = new HashMap<String, Guild>();

		this.timer = new Timer();

		this.playerManager = new DefaultAudioPlayerManager();

		this.ready = false;
		
	}
	
	/**
	 * This method <u><b>must</b></u> be called to start DiscLoader.
	 * <br>As it begins the setup process for DiscLoader to be able to function correctly<br>
	 * It <u><b>will</b></u> crash otherwise
	 * 
	 * @author Perry Berman
	 * @since 0.0.3
	 */
	public void startup() {
        if (Main.usegui) {
            Main.window = new WindowFrame(this);
        } else {
            ProgressLogger.stage(1, 3, "Mod Discovery");
            ModDiscoverer.checkModDir();
            ArrayList<ModCandidate> candidates = ModDiscoverer.discoverMods();
            TimerTask checkCandidates = new TimerTask() {

                @Override
                public void run() {
                    ProgressLogger.stage(2, 3, "Discovering Mod Containers");
                    ModRegistry.checkCandidates(candidates);
                }

            };
            this.timer.schedule(checkCandidates, 500);
        }
	}
	
	

	/**
	 * Connects the current instance of the {@link DiscLoader loader} into
	 * Discord's gateway servers
	 * 
	 * @param token your API token
	 * @return {@literal CompletableFuture<String>}
	 */
	public CompletableFuture<String> login(String token) {
		this.token = token;
		CompletableFuture<String> future;
		future = this.rest.makeRequest(Constants.Endpoints.gateway, Constants.Methods.GET, true);

		future.thenAcceptAsync(text -> {
			Gson gson = new Gson();
			Gateway gateway = gson.fromJson(text, Gateway.class);
			try {
				this.socket.connectSocket(gateway.url + "?v=6&encoding=json");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return future;
	}

	public class Gateway {
		public String url;
		public int shards;
	}

	/**
	 * 
	 * @param event
	 * @param data
	 */
	public void emit(String event, Object data) {
		for (ModContainer mod : ModRegistry.mods.values()) {
			mod.emit(event, data);
		}
	}

	/**
	 * @param event
	 */
	public void emit(String event) {
		this.emit(event, null);
	}

	public static void addEventHandler(IEventListener e) {
		handlers.put(e.toString(), e);
	}

	public Guild addGuild(GuildJSON guild) {
		boolean exists = this.guilds.containsKey(guild.id);

		Guild newGuild = new Guild(this, guild);
		this.guilds.put(newGuild.id, newGuild);
		if (!exists && this.socket.status == Constants.Status.READY) {
			this.emit(Constants.Events.GUILD_CREATE, newGuild);
		}
		return newGuild;
	}

	public Channel addChannel(ChannelJSON data) {
		return this.addChannel(data, null);
	}

	public Channel addChannel(ChannelJSON data, Guild guild) {
		boolean exists = this.channels.containsKey(data.id);
		Channel channel = null;
		if (data.type == Constants.ChannelTypes.DM) {
			channel = new PrivateChannel(this, data);
		} else if (data.type == Constants.ChannelTypes.groupDM) {
			channel = new Channel(this, data);
		} else {
			if (guild != null) {
				if (data.type == Constants.ChannelTypes.text) {
					channel = new TextChannel(guild, data);
					guild.textChannels.put(channel.id, (TextChannel) channel);
				} else if (data.type == Constants.ChannelTypes.voice) {
					channel = new VoiceChannel(guild, data);
					guild.voiceChannels.put(channel.id, (VoiceChannel) channel);
				}
			}
		}

		if (channel != null) {
			switch (channel.getType()) {
			case TEXT:
				this.textChannels.put(channel.id, (TextChannel) channel);
				break;
			case DM:
				this.privateChannels.put(channel.id, (PrivateChannel) channel);
				break;
			case VOICE:
				this.voiceChannels.put(channel.id, (VoiceChannel) channel);
				break;
			default:
				this.channels.put(channel.id, channel);
			}
			this.channels.put(channel.id, channel);
			if (!exists && this.ready) {
				this.emit(Constants.Events.CHANNEL_CREATE, channel);
			}
			return channel;
		}

		return null;
	}

	public User addUser(UserJSON data) {
		if (this.users.containsKey(data.id))
			return this.users.get(data.id);
		User user = new User(this, data);
		this.users.put(user.id, user);
		return user;
	}

	public void checkReady() {
		if (this.socket.status != Constants.Status.READY && this.socket.status != Constants.Status.NEARLY) {
			int unavailable = 0;
			for (Guild guild : this.guilds.values()) {
				unavailable += guild.available ? 0 : 1;
			}
			ProgressLogger.progress(this.guilds.size() - unavailable, this.guilds.size(), "Guilds Cached");
			if (unavailable == 0) {
				for (Guild guild : this.guilds.values()) {
					if (guild.memberCount != guild.members.size() && !guild.large) {
					}
				}

				this.socket.status = Constants.Status.NEARLY;
				this.emitReady();
			}
		}
	}

	public void emitReady() {
		this.socket.status = Constants.Status.READY;
		this.ready = true;
		CommandHandler.handleCommands = true;
		this.emit(Constants.Events.READY, this);
		Main.window.postInit();
		for (IEventListener e : handlers.values()) {
			e.Ready(this);
		}
	}

}
