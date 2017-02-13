package io.disc.discloader.objects.gateway;

public class MessageJSON {
	public String id;
	public String content;
	public String channel_id;
	public String timestamp;
	public String edited_timestamp;
	public String nonce;
	public String webhook_id;

	public boolean tts;
	public boolean mention_everyone;
	public boolean pinned;

	public UserJSON author;

	public UserJSON[] mentions;
	public RoleJSON[] mention_roles;
	
	public EmbedJSON[] embeds;

}