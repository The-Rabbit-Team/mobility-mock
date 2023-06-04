package me.vinceh121.mobilitymock;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;

import org.junit.jupiter.api.Test;

import me.vinceh121.mobilitymock.client.Client;

class TestAnon {

	@Test
	void testUuid() {
		byte[] salt = new byte[32];
		new SecureRandom(new byte[0]).nextBytes(salt);

		assertTrue(Client.anonymizeUuids("SKO-E-7bac9e0c-d05d-404c-8199-eb492d535d29", salt).startsWith("SKO-E-"));
		assertTrue(Client.anonymizeUuids("TCTU-SKO-E-80da98b3-0955-4140-b8fc-2cb50db62dbb", salt)
				.startsWith("TCTU-SKO-E-"));
		assertTrue(Client.anonymizeUuids("PSKO-P-80da98b3-0955-4140-b8fc-2cb50db62dbb", salt).startsWith("PSKO-P-"));

		String uuid = Client.anonymizeUuids(
				"SKO-E-d46c3d62-2581-4097-b561-8833ec8394e8-47135-author-ASKO-P-ecbc4435-cd86-45aa-92b5-d07f5b8d4c6c",
				salt);
		assertTrue(uuid.startsWith("SKO-E-") && uuid.contains("-author-ASKO-P-"));
		assertTrue(Client.anonymizeUuids(
				"SKO-E-d46c3d62-2581-4097-b561-8833ec8394e8-47135-author-ASKO-P-ecbc4435-cd86-45aa-92b5-d07f5b8d4c6c",
				salt).startsWith("SKO-E-"));
	}
}
