package io.discloader.discloader.network.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

import io.discloader.discloader.common.structures.SendableMessage;
import io.discloader.discloader.util.Constants;

public class APIRequest {
	public String url;

	public String route;

	public int method;

	public boolean auth;

	public final boolean multi;

	public Object data;

	public CompletableFuture<String> future;

	/**
	 * @param url
	 * @param method
	 * @param auth
	 * @param data
	 */
	public APIRequest(String url, int method, boolean auth, Object data) {
		this.url = url;
		this.method = method;
		this.auth = auth;
		this.data = data;
		this.route = this.getRoute(this.url);
		if (data != null && this.data instanceof SendableMessage && ((SendableMessage) this.data).file != null) {
			System.out.println(Constants.gson.toJson(data));
			this.multi = true;
		} else {
			this.multi = false;
		}
	}

	/**
	 * Converts the {@link APIRequest} URL to the corresponding API Endpoint
	 * 
	 * @param url
	 * @return API Endpoint {@link Constants.Endpoints}
	 */
	public String getRoute(String url) {
		String route = url.split("[?]")[0];
		if (route.contains("/channels/") || route.contains("/guilds/")) {
			int startInd = route.contains("/channels/") ? route.indexOf("/channels/") : route.indexOf("/guilds/");
			String majorID = route.substring(startInd).split("/")[2];
			route = route.replaceAll("(\\d{8,})/g", ":id").replaceAll(":id", majorID);
		}
		return route;
	}

	public CompletableFuture<?> setFuture(CompletableFuture<String> future) {
		this.future = future;
		return future;
	}

	public BaseRequest createRequest() {
		BaseRequest request = null;
		switch (this.method) {
		case Constants.Methods.GET:
			request = Unirest.get(this.route);
			break;
		case Constants.Methods.POST:
			request = Unirest.post(this.route);
			if (this.data instanceof SendableMessage && ((SendableMessage) this.data).file != null) {
				SendableMessage data = (SendableMessage) this.data,
						dData = new SendableMessage(data.content, data.embed, data.attachment, null);
				File file = data.file;
				System.out.println(dData.file == null);
				String payload = String.format(
						"------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"\"; filename=\"%s\"\r\nContent-Type: image/png\r\n\r\n\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"payload_json\"\r\n\r\n%s\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--",
						file.getPath(), Constants.gson.toJson(dData));
				System.out.println(payload);

				// byte[] fileBytes =
				// Files.readAllBytes(Paths.get(file.getPath()));
				// byte[] array = new byte[fileBytes.length +
				// payload.getBytes().length];
				// System.arraycopy(array, 0, array, 0, array.length);
				// System.arraycopy(payload.getBytes(), 0, array, array.length,
				// payload.getBytes().length);
				((HttpRequestWithBody) request).header("accept", "application/json").header("cache-control", "no-cache")
						.header("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
						.body(payload);

			} else {
				((HttpRequestWithBody) request).body(Constants.gson.toJson(this.data));
			}
			break;
		case Constants.Methods.PATCH:
			request = Unirest.patch(this.route);
			((HttpRequestWithBody) request).body(Constants.gson.toJson(this.data));
			break;
		case Constants.Methods.DELETE:
			request = Unirest.delete(this.route);
			break;
		case Constants.Methods.PUT:
			request = Unirest.put(this.route);
			((HttpRequestWithBody) request).body(Constants.gson.toJson(this.data));
			break;
		default:
			request = Unirest.get(this.route);
		}

		return request;
	}

}
