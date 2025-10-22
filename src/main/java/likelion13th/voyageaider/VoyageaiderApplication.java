package likelion13th.voyageaider;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VoyageaiderApplication {

	public static void main(String[] args) {

		// ⬇️ (추가) SpringApplication.run() *전에* .env 파일을 강제 로드
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});
		// ⬆️ 여기까지 추가

		SpringApplication.run(VoyageaiderApplication.class, args);
	}
}
