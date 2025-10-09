// TrustlikepfApplication.java
package com.picsy.trustlikepf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // ★

@SpringBootApplication
@EnableScheduling // ★
public class TrustlikepfApplication {
  public static void main(String[] args) {
    SpringApplication.run(TrustlikepfApplication.class, args);
  }
}
