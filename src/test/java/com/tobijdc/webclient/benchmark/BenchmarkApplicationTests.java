package com.tobijdc.webclient.benchmark;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

@SpringBootTest
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@ExtendWith(SpringExtension.class)
public class BenchmarkApplicationTests extends AbstractBenchmark {

    private static final Duration TEN_SECONDS = Duration.ofSeconds(10L);
    private ObjectMapper objectMapper;
    private Map<String, Object> map;
    private RandomGenerator rand;
    private ExchangeStrategies strategies;

    @Param({"10", "100", "1000", "10000",})
    public int mapSize;
    @Param({"false", "true"})
    public boolean blackbird;

	@Setup(Level.Trial)
    public void setupBenchmark() {
        objectMapper = new ObjectMapper();
        if (blackbird) {
            objectMapper.registerModule(new BlackbirdModule());
        }
        map = HashMap.newHashMap(3*mapSize);
        rand = RandomGeneratorFactory.of("Xoroshiro128PlusPlus").create(1234567890);
        generateBigMap();
        strategies = ExchangeStrategies
            .builder()
            .codecs(clientDefaultCodecsConfigurer -> {
                clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));

            }).build();
    }

    private void generateBigMap() {
        for (int i = 0; i < mapSize; i++) {
            map.put("s" + i, "i" + i + "i*i" + i*i);
            map.put("s2" + i, "i2" + i*2 + "i*i*2" + i*i*2);
            map.put("l" + i, rand.nextLong());
        }
    }

	@Benchmark
	public void createStringFirst(Blackhole blackhole) throws JsonProcessingException {
		String response = WebClient.builder().exchangeStrategies(strategies).baseUrl("http://localhost:80").build()
            .method(HttpMethod.POST)
            .contentType(MediaType.APPLICATION_JSON)
		    .bodyValue(objectMapper.writeValueAsString(map))
            .retrieve()
            .bodyToMono(String.class)
            .timeout(TEN_SECONDS)
            .block();

        blackhole.consume(response);
	}

    @Benchmark
	public void useObject(Blackhole blackhole) {
		String response = WebClient.builder().exchangeStrategies(strategies).baseUrl("http://localhost:80").build()
            .method(HttpMethod.POST)
            .contentType(MediaType.APPLICATION_JSON)
		    .bodyValue(map)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(TEN_SECONDS)
            .block();

        blackhole.consume(response);
	}

}
