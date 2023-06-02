package me.vinceh121.skolengomock;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 * CRUD operations respectively are mapped to the HTTP methods POST, GET, PATCH,
 * DELETE
 */
public abstract class AbstractCRUDRoute extends AbstractRoute {
	private static final String ERR_METHOD_NOT_ALLOWED = "Method not allowed",
			ERR_METHOD_NOT_ALLOWED_DESC = "The requested method is not supported for the requested resource";

	public AbstractCRUDRoute(SkolengoMock mock) {
		super(mock);
	}

	@Override
	public void handle(RoutingContext ctx) {
		if (ctx.request().method() == HttpMethod.POST) {
			this.handleCreate(ctx);
		} else if (ctx.request().method() == HttpMethod.GET) {
			this.handleRead(ctx);
		} else if (ctx.request().method() == HttpMethod.PATCH) {
			this.handleUpdate(ctx);
		} else if (ctx.request().method() == HttpMethod.DELETE) {
			this.handleDelete(ctx);
		} else {
			this.error(ctx, 405, ERR_METHOD_NOT_ALLOWED, ERR_METHOD_NOT_ALLOWED_DESC);
		}
	}

	protected void handleCreate(RoutingContext ctx) {
		this.error(ctx, 405, ERR_METHOD_NOT_ALLOWED, ERR_METHOD_NOT_ALLOWED_DESC);
	}

	protected void handleRead(RoutingContext ctx) {
		this.error(ctx, 405, ERR_METHOD_NOT_ALLOWED, ERR_METHOD_NOT_ALLOWED_DESC);
	}

	protected void handleUpdate(RoutingContext ctx) {
		this.error(ctx, 405, ERR_METHOD_NOT_ALLOWED, ERR_METHOD_NOT_ALLOWED_DESC);
	}

	protected void handleDelete(RoutingContext ctx) {
		this.error(ctx, 405, ERR_METHOD_NOT_ALLOWED, ERR_METHOD_NOT_ALLOWED_DESC);
	}
}
