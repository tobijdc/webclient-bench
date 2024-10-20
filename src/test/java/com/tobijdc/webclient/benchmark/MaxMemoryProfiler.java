package com.tobijdc.webclient.benchmark;

import java.util.Collection;
import java.util.List;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;

public class MaxMemoryProfiler implements InternalProfiler {

    @Override
    public String getDescription() {
        return "Max memory heap profiler";
    }

    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {

    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams,
        IterationResult result) {

        long totalHeap = Runtime.getRuntime().totalMemory();
        
        return List.of(
            new ScalarResult("Max memory heap", totalHeap, "bytes", AggregationPolicy.MAX),
            new ScalarResult("Average max memory heap", totalHeap, "bytes", AggregationPolicy.AVG)
        );
    }
}