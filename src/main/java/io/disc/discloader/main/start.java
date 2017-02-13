package io.disc.discloader.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.tree.DefaultTreeModel;

import com.google.gson.Gson;

import io.disc.discloader.DiscLoader;
import io.disc.discloader.events.GuildMemberUpdateEvent;
import io.disc.discloader.events.MessageCreateEvent;
import io.disc.discloader.events.UserUpdateEvent;
import io.disc.discloader.objects.annotations.EventHandler;
import io.disc.discloader.objects.structures.Message;
import io.disc.discloader.objects.window.WindowFrame;

/**
 * DiscLoader client entry point
 * 
 * @author Perry Berman
 * @see DiscLoader
 */
public class start {
	public static final Gson gson = new Gson();
	public static WindowFrame window;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String... args) throws IOException {
		String content = "";
		Object[] lines = Files.readAllLines(Paths.get("./options.json")).toArray();
		for (Object line : lines)
			content += line;
		options options = gson.fromJson(content, options.class);
		DiscLoader loader = new DiscLoader();
		if (args.length == 0 || !args[0].equals("nogui"))
			window = new WindowFrame(loader);
//		loader.login(options.auth.token);

	}

	@EventHandler
	public void ready(DiscLoader loader) {
		System.out.println("Test");
		loader.user.setGame("test game please ignore: TGPI");
	}

	@EventHandler
	public void MessageCreate(MessageCreateEvent e) {

	}

	@EventHandler
	public void raw(String text) {
		System.out.println(text);
	}

	// @eventHandler
	public void debug(String debug) {
		System.out.println(debug);
	}

	// @eventHandler
	public void UserUpdate(UserUpdateEvent e) {

	}

	// @eventHandler
	public void PresenceUpdate(GuildMemberUpdateEvent e) {
		// window.panel.guilds.guilds.get(e.guild.id).updateMemberNode(e.member);
		// updateViewPanel();
	}

	public void updateViewPanel() {
		DefaultTreeModel model = (DefaultTreeModel) window.panel.root.getModel();
		// load changes to the tree
		model.nodeChanged(window.panel.rootNode);
		window.panel.root.setSelectionPath(window.panel.path);
	}
}