package io.discloader.discloader.client.command;

import java.util.ArrayList;
import java.util.Arrays;

import io.discloader.discloader.common.discovery.ModContainer;
import io.discloader.discloader.common.event.MessageCreateEvent;
import io.discloader.discloader.common.registry.ModRegistry;
import io.discloader.discloader.entity.RichEmbed;
import io.discloader.discloader.util.DLUtil;

/**
 * @author Perry Berman
 *
 */
public class CommandMods extends Command {

	private final String regex = "(.*)";

	public CommandMods() {
		super();
		setUnlocalizedName("mods").setTextureName("discloader:mods").setDescription("mods");
		setUsage("mods [<mod>]").setArgsRegex(regex);
	}

	public void execute(MessageCreateEvent e, String[] args) {
		RichEmbed embed = new RichEmbed().setColor(0x55cdF2);
		ModContainer mc;
		if (args.length == 1 && (mc = ModRegistry.mods.get(args[0])) != null) {

			if (mc != null) {
				embed.setThumbnail(DLUtil.MissingTexture);
				embed.addField("Description", mc.modInfo.desc(), true).addField("Version", mc.modInfo.version(), true)
						.addField("Author(s)", mc.modInfo.author(), true);
			}
		} else {
			ArrayList<String> modList = new ArrayList<String>();
			for (ModContainer mcs : ModRegistry.mods.values()) {
				modList.add(String.format("%s", mcs.modInfo.name()));
			}
			embed.setThumbnail(this.getIcon().getFile());
			String mods = Arrays.toString(modList.toArray());
			mods = mods.substring(1, mods.length() - 1);
			embed.addField("Mods", mods);
		}
		e.message.channel.sendEmbed(embed);
	}
}
