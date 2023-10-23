package io.eventuate.tram.spring.cloudsleuthintegration.test;

import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes= SpringCloudSleuthWhenDisabledIntegrationTest.TestConfiguration.class, webEnvironment = SpringBootTest
        .WebEnvironment.NONE, properties = "spring.sleuth.enabled=false")
public class SpringCloudSleuthWhenDisabledIntegrationTest {


  @Configuration
  @EnableAutoConfiguration
  @Import(TramInMemoryConfiguration.class)
  static class TestConfiguration {
  }


  @Test
  public void applicationContextShouldLoad() {
  }



}
