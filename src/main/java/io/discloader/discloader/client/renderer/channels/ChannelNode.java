/**
 * 
 */
package io.discloader.discloader.client.renderer.channels;

import javax.swing.tree.DefaultMutableTreeNode;

import io.discloader.discloader.common.structures.channels.Channel;

/**
 * @author Perry Berman
 *
 */
public class ChannelNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -1915867794359095355L;

	/**
	 * @param userObject
	 */
	public ChannelNode(Object userObject) {
		super(userObject);

	}

	/**
	 * @param userObject
	 * @param data
	 */
	public ChannelNode(Object userObject, Channel data) {
		super(userObject);

	}

	public void updateNode(Channel data) {

	}

}