package me.vinceh121.mobilitymock;

import static com.rethinkdb.RethinkDB.r;

import java.util.EnumSet;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.VertxModule;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import me.vinceh121.mobilitymock.SimpleCRUDRoute.CRUD;
import me.vinceh121.mobilitymock.json.JsonObjectMixin;

public class MobilityMock {
	private static final Logger LOG = LogManager.getLogger(MobilityMock.class);
	private final Vertx vertx;
	private final HttpServer server;
	private final Router routerRoot, routerApi;
	private final Connection conn;

	public static void main(final String[] args) {
		// allows (de)serialization of VertX's JsonObject on Rethink's side
		RethinkDB.getResultMapper().registerModule(new VertxModule());
		RethinkDB.getResultMapper().addMixIn(JsonObject.class, JsonObjectMixin.class);

		final MobilityMock mock = new MobilityMock();
		mock.start();
	}

	public MobilityMock() {
		this.vertx = Vertx.vertx();
		this.server = this.vertx.createHttpServer();
		this.routerRoot = Router.router(this.vertx);
		this.routerApi = Router.router(this.vertx);
		this.routerRoot.route("/api/v1/bff-sko-app/*").subRouter(this.routerApi);
		this.server.requestHandler(this.routerRoot);

		this.conn = r.connection(Objects.requireNonNull(System.getenv("MOCK_DB_URL"), "Missing MOCK_DB_URL envvar"))
				.connect();

		this.registerRoutes();
	}

	private void registerRoutes() {
		this.registerApiRoute("/schools", new SimpleCRUDRoute(this, "schools", EnumSet.of(CRUD.READ)));
	}

	private void registerApiRoute(String path, Handler<RoutingContext> handler) {
		this.routerApi.route(path)
				.handler(BodyHandler.create(false).setBodyLimit(524288)) // do not permit file upload, allow max 512 kiB
																			// bodies
				.handler(ctx -> {
					ctx.response().putHeader("Content-Type", "application/vnd.api+json");
					ctx.next();
				})
				.handler(handler);
	}

	public void start() {
		final String host = System.getenv("MOCK_HOST") != null ? System.getenv("MOCK_HOST") : "0.0.0.0";
		final int port = Integer
						.parseInt(Objects.requireNonNull(System.getenv("MOCK_PORT"), "Missing MOCK_PORT envvar"));
		this.server
				.listen(port, host)
				.onSuccess(s -> LOG.info("Started! Listening on {}", s.actualPort()))
				.onFailure(t -> LOG.error("Failed to start http server", t));
	}

	public Vertx getVertx() {
		return this.vertx;
	}

	public Router getRouterApi() {
		return this.routerApi;
	}

	public Connection getConn() {
		return this.conn;
	}
}
