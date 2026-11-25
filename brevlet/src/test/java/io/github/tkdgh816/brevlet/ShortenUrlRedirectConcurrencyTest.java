package io.github.tkdgh816.brevlet;

import io.github.tkdgh816.brevlet.application.ShortenUrlService;
import io.github.tkdgh816.brevlet.dto.url.CreateShortenUrlRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("h2")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:redirect_concurrency"
)
class ShortenUrlRedirectConcurrencyTest {

    private static final int REQUEST_COUNT = 20;
    private static final String ORIGINAL_URL = "https://example.com/article";

    @LocalServerPort
    private int port;

    @Autowired
    private ShortenUrlService shortenUrlService;

    @Test
    @DisplayName("같은 링크에 리다이렉트 요청 20건이 동시에 성공하면 조회 수는 20이다")
    void concurrentRedirects_incrementCountExactly20Times() throws Exception {
        var created = shortenUrlService.generateShortenUrl(new CreateShortenUrlRequest(ORIGINAL_URL, null));
        String key = created.shortenUrlKey();
        assertThat(shortenUrlService.getShortenUrlInfoByKey(key).redirectCount())
                .as("요청 전 조회 수").isZero();

        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/" + key))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        var startBarrier = new CyclicBarrier(REQUEST_COUNT);

        try (var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
             var workers = Executors.newFixedThreadPool(REQUEST_COUNT)) {
            List<Future<HttpResponse<Void>>> responses = new ArrayList<>(REQUEST_COUNT);
            for (int i = 0; i < REQUEST_COUNT; i++) {
                responses.add(workers.submit(() -> {
                    startBarrier.await(10, TimeUnit.SECONDS);
                    return client.send(request, HttpResponse.BodyHandlers.discarding());
                }));
            }

            for (int i = 0; i < responses.size(); i++) {
                var response = responses.get(i).get(45, TimeUnit.SECONDS);
                assertThat(response.statusCode()).as("%d번째 요청의 상태 코드", i + 1).isEqualTo(302);
                assertThat(response.headers().firstValue("Location"))
                        .as("%d번째 요청의 리다이렉트 목적지", i + 1).contains(ORIGINAL_URL);
            }
        }

        assertThat(shortenUrlService.getShortenUrlInfoByKey(key).redirectCount())
                .as("키 %s에 대한 리다이렉트 요청 %d건 완료 후 조회 수", key, REQUEST_COUNT)
                .isEqualTo((long) REQUEST_COUNT);
    }
}
