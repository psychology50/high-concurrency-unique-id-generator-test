package kr.co.idgenerator;

import org.junit.jupiter.api.Test;

public class IdGeneratorBenchmarkTest {
    private static final int[] SAMPLE_SIZES = {256, 512, 1_024, 4_096, 8_192, 100_000, 300_000, 500_000};
    private static final int THREAD_COUNT = 10;

    @Test
    public void test() {
        System.out.println("Hello, World!");
    }
}
