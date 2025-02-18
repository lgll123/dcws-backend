package com.formssi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 启动程序 启动时加入--add-opens java.base/java.lang=ALL-UNNAMED 以兼容高版本JDK
 * <p>
 * 例如java --add-opens java.base/java.lang=ALL-UNNAMED -jar low-code-admin.jar
 * </p>
 *
 * @author Lion Li
 */

@SpringBootApplication
public class DcwsApplication {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(DcwsApplication.class);
    application.setApplicationStartup(new BufferingApplicationStartup(2048));
    application.run(args);
    System.out.println("(♥◠‿◠)ﾉﾞ  DcwsApplication启动成功   ლ(´ڡ`ლ)ﾞ");
  }

}
