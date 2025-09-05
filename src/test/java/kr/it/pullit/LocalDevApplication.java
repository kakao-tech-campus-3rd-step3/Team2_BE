package kr.it.pullit;

import kr.it.pullit.boot.LocalDevTestcontainersConfig;
import org.springframework.boot.SpringApplication;

public class LocalDevApplication {

  public static void main(String[] args) {
    SpringApplication.from(PullitApplication::main)
        .with(LocalDevTestcontainersConfig.class)
        .run(args);
  }
}
