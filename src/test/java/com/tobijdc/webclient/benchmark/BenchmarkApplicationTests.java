package com.tobijdc.webclient.benchmark;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@ExtendWith(SpringExtension.class)
class BenchmarkApplicationTests extends AbstractBenchmark {

    private static final Duration FIVE_SECONDS = Duration.ofSeconds(5L);

	@Setup(Level.Trial)
    public void setupBenchmark() {
         WebClient.create("url").method(HttpMethod.POST)
		    .bodyValue("abc")
            .retrieve()
            .bodyToMono(Object.class)
            .timeout(FIVE_SECONDS);
    }

	@Test
	void contextLoads() {
		
	}

}
