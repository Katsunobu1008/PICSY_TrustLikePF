// TrustlikepfApplicationTests.java
package com.picsy.trustlikepf;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import com.picsy.trustlikepf.domain.repository.SettingsRepository;
import com.picsy.trustlikepf.domain.repository.TransactionLogRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;
@SpringBootTest(properties = {
	"spring.flyway.enabled=false",
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@Import(TrustlikepfApplicationTests.RepositoryMocks.class)
@SuppressWarnings("unused")
class TrustlikepfApplicationTests {

	@TestConfiguration(proxyBeanMethods = false)
	@SuppressWarnings("unused")
	static class RepositoryMocks {

		@Bean SettingsRepository settingsRepository() {
			return Mockito.mock(SettingsRepository.class);
		}

		@Bean EvaluationMatrixRepository evaluationMatrixRepository() {
			return Mockito.mock(EvaluationMatrixRepository.class);
		}

		@Bean ContributionVectorRepository contributionVectorRepository() {
			return Mockito.mock(ContributionVectorRepository.class);
		}

		@Bean PostRepository postRepository() {
			return Mockito.mock(PostRepository.class);
		}

		@Bean TransactionLogRepository transactionLogRepository() {
			return Mockito.mock(TransactionLogRepository.class);
		}

		@Bean UserRepository userRepository() {
			return Mockito.mock(UserRepository.class);
		}
	}

	@Test
	void contextLoads() {
	}

}
