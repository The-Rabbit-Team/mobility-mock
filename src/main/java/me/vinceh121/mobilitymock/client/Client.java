package me.vinceh121.mobilitymock.client;

import static com.rethinkdb.RethinkDB.r;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.person.Person;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Result;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class Client {
	public static final String HOST = "api.skolengo.com";
	public static final Pattern PAT_UUID
			= Pattern.compile("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}");
	public static final String[] DB_TABLES = { "schools" };
	public static final String[] SCHOOL_TYPES = { "Collège", "Lycée" };
	private final Vertx vertx;
	private final WebClient client;
	private final byte[] salt;
	private final Fairy fairy = Fairy.create(Locale.FRENCH);
	private String token;
	private Connection conn;

	public static void main(String[] args) {
		Client c = new Client();
		c.start(List.of(args));
	}

	public Client() {
		this.vertx = Vertx.vertx();
		this.client = WebClient.create(vertx);

		this.salt = new byte[32];
		new SecureRandom().nextBytes(salt);
	}

	public void start(List<String> args) {
		CLI cli = CLI.create("client")
				.addOption(new Option().setLongName("db").setDefaultValue("rethinkdb://localhost/mobilitymock"))
				.addOption(new Option().setLongName("reset").setFlag(true))
				.addOption(new Option().setLongName("fetch").setFlag(true))
				.addOption(new Option().setLongName("anonymize").setFlag(true))
				.addOption(new Option().setLongName("token"))
				.addOption(new Option().setLongName("help").setShortName("h").setHelp(true));
		CommandLine cl = cli.parse(args);

		if (!cl.isValid() || cl.isAskingForHelp()) {
			StringBuilder sb = new StringBuilder();
			cli.usage(sb);
			System.out.println(sb);
			return;
		}

		this.conn = r.connection(cl.<String>getOptionValue("db")).connect();

		this.token = cl.getOptionValue("token");

		if (cl.isFlagEnabled("reset")) {
			for (String table : DB_TABLES) {
				r.tableDrop(table).run(this.conn);
			}
		}

		try (Result<List<String>> res = r.tableList().run(conn, new TypeReference<List<String>>() {})) {
			List<String> existingTables = res.first();
			for (String table : DB_TABLES) {
				if (!existingTables.contains(table)) {
					r.tableCreate(table).run(conn);
				}
			}
		}

		Future.succeededFuture().compose(v -> {
			return cl.isFlagEnabled("fetch") ? this.fetch() : Future.succeededFuture();
		}).compose(v -> {
			return cl.isFlagEnabled("anonymize") ? this.anonymize() : Future.succeededFuture();
		}).onSuccess(f -> {
			this.conn.close();
			vertx.close().onSuccess(v -> {
				System.out.println("Done!");
			});
		}).onFailure(t -> {
			t.printStackTrace();
			System.exit(-1);
		});
	}

	private CompositeFuture fetch() {
		List<Future<?>> futures = new ArrayList<>();

		futures.add(this.request(HttpMethod.GET, "/schools?filter[text]=France&page[limit]=100").send().andThen(res -> {
			JsonArray data = res.result().body().getJsonArray("data");

			for (int i = 0; i < data.size(); i++) {
				JsonObject obj = data.getJsonObject(i);
				r.table("schools").insert(obj.mapTo(Map.class)).run(this.conn);
			}
		}));

		return Future.all(futures);
	}

	private CompositeFuture anonymize() {
		List<Future<?>> futures = new ArrayList<>();

		futures.add(
				Future.fromCompletionStage(r.table("schools").runAsync(this.conn, ObjectNode.class)).andThen(res -> {
					try (Result<ObjectNode> rres = res.result()) {
						for (ObjectNode sch : rres) {
							String oldId = sch.get("id").asText();
							sch.put("id", anonymizeUuids(oldId, salt));

							ObjectNode attr = (ObjectNode) sch.get("attributes");

							Person p = this.fairy.person();
							String schoolType = randomSchoolType();

							attr.put("addressLine1", p.getAddress().getAddressLine1());
							attr.put("addressLine2", p.getAddress().getAddressLine2());
							attr.putNull("addressLine3");
							attr.put("zipCode", p.getAddress().getPostalCode());
							attr.put("city", p.getAddress().getCity());
							attr.put("name", schoolType + " " + p.getFullName());
							attr.put("homePageUrl", anonymizeCasUrl(attr.get("homePageUrl").asText()));

							r.table("schools").get(oldId).delete().run(this.conn);
							r.table("schools").insert(sch).run(this.conn);
						}
					}
				}));

		return Future.all(futures);
	}

	public HttpRequest<JsonObject> request(HttpMethod method, String path) {
		return this.client.request(method, 443, HOST, "/api/v1/bff-sko-app" + path)
				.ssl(true)
				.putHeader("Authorization", "Bearer " + this.token)
				.as(BodyCodec.jsonObject());
	}

	public byte[] getSalt() {
		return salt;
	}

	public Vertx getVertx() {
		return vertx;
	}

	public WebClient getClient() {
		return client;
	}

	public static String randomSchoolType() {
		Random r = new SecureRandom();
		return SCHOOL_TYPES[r.nextInt(SCHOOL_TYPES.length)];
	}

	public static String anonymizeCasUrl(String spec) {
		return spec.split(Pattern.quote("?"))[0];
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
