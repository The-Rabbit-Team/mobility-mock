package me.vinceh121.mobilitymock.json;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Jackson Mixin to allow LinkedHashMap -> Vertx JsonObject
 */
public abstract class JsonObjectMixin {
	@JsonCreator
	public JsonObjectMixin(Map<String, Object> map) {
	}
}
