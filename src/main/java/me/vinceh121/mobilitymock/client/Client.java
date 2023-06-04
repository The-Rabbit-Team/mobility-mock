package me.vinceh121.mobilitymock.client;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import io.vertx.ext.web.client.WebClient;

public class Client {
	private static final Pattern PAT_UUID
			= Pattern.compile("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}");
	private final Vertx vertx;
	private final WebClient client;
	private byte[] salt;

	public Client() {
		this.vertx = Vertx.vertx();
		this.client = WebClient.create(vertx);

		this.salt = new byte[32];
		new SecureRandom().nextBytes(salt);
	}

	public void start(List<String> args) {
		CLI cli = CLI.create("client")
				.addOption(new Option().setLongName("fetch").setFlag(true))
				.addOption(new Option().setLongName("anonymize").setFlag(true))
				.addOption(new Option().setLongName("help").setShortName("h").setHelp(true));
		CommandLine cl = cli.parse(args);

		if (!cl.isValid() || cl.isAskingForHelp()) {
			StringBuilder sb = new StringBuilder();
			cli.usage(sb);
			System.out.println(sb);
			return;
		}

		@SuppressWarnings("rawtypes") // god damnit vertx!
		List<Future> futures = new ArrayList<>(2);

		if (cl.isFlagEnabled("fetch")) {
			futures.add(this.fetch());
		}
		if (cl.isFlagEnabled("anonymize")) {
			futures.add(this.anonymize());
		}

		CompositeFuture.all(futures).onSuccess(f -> System.out.println("Done!")).onFailure(System.out::println);
	}

	private Future<Void> fetch() {
		return Future.future(p -> {

		});
	}

	private Future<Void> anonymize() {
		return Future.future(p -> {

		});
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public Vertx getVertx() {
		return vertx;
	}

	public WebClient getClient() {
		return client;
	}

	public static String anonymizeUuids(String id, byte[] salt) {
		StringBuilder sb = new StringBuilder(id);
		Matcher m = PAT_UUID.matcher(id);
		while (m.find()) {
			sb.replace(m.start(), m.end(), anonymizeUuid(id, salt));
		}
		return sb.toString();
	}

	public static String anonymizeUuid(String id, byte[] salt) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(salt);
			LongBuffer buf = ByteBuffer.wrap(digest.digest(id.getBytes())).asLongBuffer();
			UUID hashedUuid = new UUID(buf.get(), buf.get());
			return hashedUuid.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
