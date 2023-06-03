package me.vinceh121.mobilitymock;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractRoute implements Handler<RoutingContext> {
	protected final MobilityMock mock;

	/**
	 * @param mock  Main instance of the mocking server
	 * @param table RethinkDB table to r/w data from/to
	 * @param crud  Set of CRUD operations that are allowed
	 */
	public AbstractRoute(MobilityMock mock) {
		this.mock = mock;
	}

	public void error(RoutingContext ctx, int status, String title, String desc) {
		// @formatter:off
		ctx.response()
				.end(new JsonObject()
						.put("errors", new JsonArray()
								.add(new JsonObject()
										.put("status", status)
										.put("title", title)
										.put("details", desc)))
						.toBuffer());
		// @formatter:on
	}
}
