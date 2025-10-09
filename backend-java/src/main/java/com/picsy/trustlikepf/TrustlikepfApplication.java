// TrustlikepfApplication.java
package com.picsy.trustlikepf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;   // ★追加

@SpringBootApplication
@EnableScheduling   // ★追加
public class TrustlikepfApplication {
  public static void main(String[] args) {
    SpringApplication.run(TrustlikepfApplication.class, args);
  }
}
