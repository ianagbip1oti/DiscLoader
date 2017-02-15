package io.discloader.discloader.socket;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import io.discloader.discloader.DiscLoader;
import io.discloader.discloader.util.constants;

/**
 * @author Perry Berman
 *
 */
public class DiscSocket {

	/**
	 * 
	 */
	public DiscLoader loader;

	private DiscSocketListener socketListener;
	/**
	 * 
	 */
	public WebSocket ws;

	public String sessionID;

	public int s;
	public int status;

	public boolean lastHeartbeatAck;
	public boolean first = true;
	public boolean normalReady = false;
	public boolean reconnecting = false;

	private TimerTask heartbeatInterval;

	public Timer timer = new Timer();

	public Gson gson = new Gson();

	/**
	 * @param loader
	 */
	public DiscSocket(DiscLoader loader) {
		this.loader = loader;

		this.socketListener = new DiscSocketListener(this);

		this.status = constants.Status.IDLE;
	}

	/**
	 * @throws IOException
	 * @throws WebSocketException
	 * 
	 */
	public void connectSocket(String gateway) throws WebSocketException, IOException {
		this.ws = new WebSocketFactory().setConnectionTimeout(15000).createSocket(gateway).addHeader("Accept-Encoding",
				"gzip");
		this.ws.addListener(this.socketListener);
		this.ws.connect();
	}

	public void keepAlive(int interval) {
		DiscSocket socket = this;
		this.heartbeatInterval = new TimerTask() {
			@Override
			public void run() {
				socket.loader.emit("debug", "Sending heartbeat");
				socket.sendHeartbeat(true);
			}
		};
		timer.scheduleAtFixedRate(this.heartbeatInterval, interval, interval);
	}

	public void sendHeartbeat(boolean normal) {
		if (normal && !this.lastHeartbeatAck) {
			this.ws.disconnect(1007);
			return;
		}
		this.loader.emit("debug", "Attempting heartbeat");
		JSONObject payload = new JSONObject();
		payload.put("op", constants.OPCodes.HEARTBEAT).put("d", this.s);
		this.send(payload, true);
		this.lastHeartbeatAck = false;
	}

	public void send(JSONObject payload, boolean force) {
		this.ws.sendText(payload.toString());
	}

	public void send(JSONObject packet) {
		this.send(packet, false);
	}
}