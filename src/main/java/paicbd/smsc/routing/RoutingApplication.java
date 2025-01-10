package paicbd.smsc.routing;

import com.paic.licenser.licenseValidator;
import com.paicbd.smsc.utils.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@Generated
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class RoutingApplication {
	public static void main(String[] args) {
		Thread checkLicense = new Thread(() -> {
			try {
				licenseValidator checker = new licenseValidator();
				checker.validate();
			} catch (Exception e) {
				log.info("Exception found during startup : {}", e.getMessage());
			}
		});
		checkLicense.start();
		SpringApplication.run(RoutingApplication.class, args);
	}
}
