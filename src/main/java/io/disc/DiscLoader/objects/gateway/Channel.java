package io.disc.DiscLoader.objects.gateway;

public class Channel {
	String id;
	String type;
	boolean is_private;
	User[] recipients;
	String last_message_id;
}