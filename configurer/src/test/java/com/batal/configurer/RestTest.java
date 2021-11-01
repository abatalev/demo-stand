package com.batal.configurer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestTest {

    @LocalServerPort
    private int port;

    @Test
    public void check() throws Exception {

        RestTemplate restTemplate = new RestTemplateBuilder(
                rt -> rt.getInterceptors()
                        .add((request, body, execution) -> {
                            request.getHeaders().add("uber-trace-id", "1:1:0:1");
                            return execution.execute(request, body);
                        })).build();

        String response = restTemplate
                .getForObject(
                        "http://localhost:" + port + "/api/config/v1/actions",
                        String.class);
        assertNotNull(response);
    }
}
