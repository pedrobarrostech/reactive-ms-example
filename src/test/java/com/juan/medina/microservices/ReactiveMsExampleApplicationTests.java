package com.juan.medina.microservices;

import com.juan.medina.microservices.model.HelloRequest;
import com.juan.medina.microservices.model.HelloResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.UriSpec;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.util.UriBuilder;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReactiveMsExampleApplicationTests {

    private static final String DEFAULT_VALUE = "world";
    private static final String CUSTOM_VALUE = "reactive";
    private static final String JSON_VALUE = "json";
    private static final String HELLO_PATH = "/hello";
    private static final String NAME_ARG = "{name}";

    private WebTestClient client;

    @Autowired
    private RouterFunction<?> helloRouterFunction;

    @Before
    public void setup() {
        client = WebTestClient.bindToRouterFunction(helloRouterFunction).build();
    }

    private FluxExchangeResult<HelloResponse> invoke(UriSpec spec, Function<UriBuilder, URI> url,
                                                     BodyInserter<HelloRequest, ReactiveHttpOutputMessage> object) {
        return spec.uri(url)
                .accept(APPLICATION_JSON_UTF8)
                .exchange(object)
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON_UTF8)
                .expectBody(HelloResponse.class)
                .returnResult();
    }

    private FluxExchangeResult<HelloResponse> invoke(UriSpec spec, Function<UriBuilder, URI> url) {
        return invoke(spec, url, null);
    }

    private void verify(FluxExchangeResult<HelloResponse> result, Consumer<HelloResponse> check) {
        StepVerifier.create(result.getResponseBody())
                .consumeNextWith(check)
                .expectComplete()
                .verify();
    }

    @Test
    public void defaultHelloTest() {

        verify(
                invoke(
                        client.get(),
                        builder -> builder.path(HELLO_PATH).build()),
                it -> {
                    assertThat(it.getHello(), is(DEFAULT_VALUE));
                }
        );
    }

    @Test
    public void getHelloTest() {
        verify(
                invoke(
                        client.get(),
                        builder -> builder.path(HELLO_PATH).path("/").path(NAME_ARG).build(CUSTOM_VALUE)),
                it -> {
                    assertThat(it.getHello(), is(CUSTOM_VALUE));
                }
        );
    }

    @Test
    public void postHelloTest() {

        verify(
                invoke(
                        client.post(),
                        builder -> builder.path(HELLO_PATH).build(),
                        BodyInserters.fromObject(new HelloRequest(JSON_VALUE))),
                it -> {
                    assertThat(it.getHello(), is(JSON_VALUE));
                }
        );
    }

}
