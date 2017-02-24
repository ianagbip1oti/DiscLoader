package io.discloader.guimod.gui.event;

import io.discloader.discloader.client.render.WindowFrame;
import io.discloader.discloader.common.DiscLoader;
import io.discloader.discloader.common.event.DLPreInitEvent;
import io.discloader.discloader.common.event.EventListenerAdapter;
import io.discloader.discloader.common.start.Main;
import io.discloader.guimod.gui.TabbedPanel;

public class GUIEvents extends EventListenerAdapter {

	private static WindowFrame window = Main.window;
	private TabbedPanel tabs;
	
	public void PreInit(DLPreInitEvent e) {
		System.out.print("Hmmm");
	}

	@Override
	public void Ready(DiscLoader loader) {
		window.remove(WindowFrame.loading);
		window.revalidate();
		window.add(this.setTabs(new TabbedPanel(loader)));
		window.revalidate();
		window.repaint();
	}

	/**
	 * @return the tabs
	 */
	public TabbedPanel getTabs() {
		return tabs;
	}

	/**
	 * @param tabs the tabs to set
	 */
	public TabbedPanel setTabs(TabbedPanel tabs) {
		this.tabs = tabs;
		return tabs;
	}
	
}