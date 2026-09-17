package com.labresource.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "jwt.secret=${JWT_SECRET:dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW1pbi0zMi1ieXRlcw==}")
class LabResourceApplicationTests {

	@Test
	void contextLoads() {
	}

}
