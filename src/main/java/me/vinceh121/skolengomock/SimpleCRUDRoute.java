package me.vinceh121.skolengomock;

import static com.rethinkdb.RethinkDB.r;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Simple CRUD route that allows the specified operations on a certain table
 * without any processing other than JSON:API formatting.
 */
public class SimpleCRUDRoute extends AbstractCRUDRoute {
	private final String table;
	private final Set<CRUD> allowedOperations;

	public SimpleCRUDRoute(SkolengoMock mock, String table, Set<CRUD> allowedOperations) {
		super(mock);
		this.table = table;
		this.allowedOperations = allowedOperations;
	}

	@Override
	protected void handleCreate(RoutingContext ctx) {
		if (!this.allowedOperations.contains(CRUD.CREATE)) {
			super.handleCreate(ctx); // differ to 405
			return;
		}

		r.table(this.table).insert(ctx.body().asJsonObject()).runAsync(this.mock.getConn()).thenAcceptAsync(res -> {
			ctx.end(); // TODO create response
		});
	}

	@Override
	protected void handleRead(RoutingContext ctx) {
		if (!this.allowedOperations.contains(CRUD.READ)) {
			super.handleRead(ctx);
			return;
		}

		// does a paginated read
		// TODO single read with path params
		final int offset = Integer.parseInt(ctx.request().getParam("page[offset]", "0"));
		final int limit = Integer.parseInt(ctx.request().getParam("page[limit]", "100"));

		if (limit > 100) {
			// TODO use actual error msgs
			this.error(ctx, 400, "Invalid parameter", "page[limit] cannot be higher than 100");
			return;
		}

		r.table(this.table) // fetches data
				.skip(offset)
				.limit(limit)
				.runAsync(this.mock.getConn(), JsonObject.class)
				.thenAcceptAsync(resData -> {
					r.table(this.table) // fetches total count of elements in the table
							.count()
							.runAsync(this.mock.getConn(), Integer.class)
							.thenAcceptAsync(resCount -> {
								List<JsonObject> list = resData.stream().collect(Collectors.toList());
								resData.close();
								int count = resCount.first();
								resCount.close();

								JsonArray data = new JsonArray(list);

								JsonObject res = new JsonObject();
								res.put("data", data);
								res.put("meta", new JsonObject().put("totalResourceCount", count));

								ctx.end(res.toBuffer());
							});
				});
	}

	@Override
	protected void handleUpdate(RoutingContext ctx) {
		throw new UnsupportedOperationException("handleUpdate not implemented");
	}
	
	@Override
	protected void handleDelete(RoutingContext ctx) {
		throw new UnsupportedOperationException("handleDelete not implemented");
	}
	
	public enum CRUD {
		CREATE, READ, UPDATE, DELETE;
	}
}
