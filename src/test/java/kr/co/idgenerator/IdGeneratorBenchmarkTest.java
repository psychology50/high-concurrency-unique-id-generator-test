package kr.co.idgenerator;

import kr.co.idgenerator.strategy.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IdGeneratorBenchmarkTest {
    private static final int THREAD_COUNT = 10;

    private static Connection conn;
    private static final Map<String, List<BenchmarkResult>> results = new HashMap<>();

    @BeforeAll
    public void setUp() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:~/test;DB_CLOSE_DELAY=-1", "sa", "1234");
        createTables();
    }

    @AfterAll
    static void teardown() throws Exception {
        dropTables();
        conn.close();
        ResultWriter.exportToExcel(results);
    }

    @ParameterizedTest(name = "UUID")
    @ValueSource(ints = {256, 512, 1_024, 4_096, 8_192, 100_000, 300_000, 500_000})
    public void evaluateUUID(int sampleSize) throws Exception {
        IdGenerator<String> generator = new UuidGenerator();
        runBenchmark(generator, "UUID", sampleSize);
    }

    @ParameterizedTest(name = "TimeOrderedUUID")
    @ValueSource(ints = {256, 512, 1_024, 4_096, 8_192, 100_000, 300_000, 500_000})
    public void evaluateTimeOrderedUUID(int sampleSize) throws Exception {
        IdGenerator<String> generator = new TimeOrderedUuidGenerator();
        runBenchmark(generator, "TimeOrderedUUID", sampleSize);
    }

    @ParameterizedTest(name = "TimeOrderedEpochUUID")
    @ValueSource(ints = {256, 512, 1_024, 4_096, 8_192, 100_000, 300_000, 500_000})
    public void evaluateTimeOrderedEpocUUID(int sampleSize) throws Exception {
        IdGenerator<String> generator = new TimeOrderedEpochUuidGenerator();
        runBenchmark(generator, "TimeOrderedEpochUUID", sampleSize);
    }

    @ParameterizedTest(name = "KSUID")
    @ValueSource(ints = {256, 512, 1_024, 4_096, 8_192, 100_000, 300_000, 500_000})
    public void evaluateKSUID(int sampleSize) throws Exception {
        IdGenerator<String> generator = new KsuidGenerator();
        runBenchmark(generator, "KSUID", sampleSize);
    }

    @ParameterizedTest(name = "ULID")
    @ValueSource(ints = {256, 512, 1_024, 4_096, 8_192, 100_000, 300_000, 500_000})
    public void evaluateULID(int sampleSize) throws Exception {
        IdGenerator<String> generator = new TsidGenerator();
        runBenchmark(generator, "ULID", sampleSize);
    }

    @ParameterizedTest(name = "TSID")
    @ValueSource(ints = {256, 512, 1_024, 4_096, 8_192, 100_000, 300_000, 500_000})
    public void evaluateTSID(int sampleSize) throws Exception {
        IdGenerator<String> generator = new TsidGenerator();
        runBenchmark(generator, "TSID", sampleSize);
    }

    @ParameterizedTest(name = "TSIDLong")
    @ValueSource(ints = {256, 512, 1_024, 4_096, 8_192, 100_000, 300_000, 500_000})
    public void evaluateTSIDLong(int sampleSize) throws Exception {
        IdGenerator<Long> generator = new TsidLongGenerator();
        runBenchmark(generator, "TSIDLong", sampleSize);
    }

    private <E extends Comparable<? super E>> void runBenchmark(IdGenerator<E> generator, String generatorName, int sampleSize) throws Exception {
        BenchmarkResult result = new BenchmarkResult(generatorName, sampleSize);
        result.setGenerationTime(testGenerationTime(generator, sampleSize));
        result.setSortable(testSortability(generator, sampleSize));
        result.setCollisionRate(testCollisionRate(generator, sampleSize));
        result.setDbJoinTime(testDbJoinPerformance(generator, generatorName, sampleSize));

        // ID의 byte 크기 계산
        E sampleId = generator.execute();
        String exampleId = sampleId.toString();
        result.setExampleId(exampleId);
        result.setByteSize(exampleId.getBytes().length);

        results.computeIfAbsent(generatorName, k -> new ArrayList<>()).add(result);
    }

    // ID 생성 시간 측정
    private <E> long testGenerationTime(IdGenerator<E> generator, int sampleSize) throws Exception {
        long start = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(sampleSize);

        for (int i = 0; i < sampleSize; i++) {
            executor.submit(() -> {
                try {
                    generator.execute();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(); // 모든 작업이 완료될 때까지 대기
        } finally {
            executor.shutdown();
        }

        return (System.nanoTime() - start) / 1_000_000; // 밀리 초로 변환
    }

    // ID 정렬 가능 여부 테스트
    private <E extends Comparable<? super E>> boolean testSortability(IdGenerator<E> generator, int sampleSize) {
        List<E> ids = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            ids.add(generator.execute());
        }
        List<E> sortedIds = new ArrayList<>(ids);
        Collections.sort(sortedIds);
        return ids.equals(sortedIds);
    }

    // ID 충돌율 테스트
    private <E> double testCollisionRate(IdGenerator<E> generator, int sampleSize) throws Exception {
        Set<E> uniqueIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(sampleSize);

        for (int i = 0; i < sampleSize; i++) {
            executor.submit(() -> {
                try {
                    uniqueIds.add(generator.execute());
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(); // 모든 작업이 완료될 때까지 대기
        } finally {
            executor.shutdown();
        }

        return 1 - ((double) uniqueIds.size() / sampleSize);
    }

    // DB 조인 성능 테스트
    private <E> long testDbJoinPerformance(IdGenerator<E> generator, String generatorName, int sampleSize) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + generatorName + "_table (id) VALUES (?)");
        for (int i = 0; i < sampleSize; i++) {
            E id = generator.execute();

            if (id instanceof String) {
                pstmt.setString(1, (String) id);
            } else if (id instanceof Long) {
                pstmt.setLong(1, (Long) id);
            }

            pstmt.executeUpdate();
        }

        long start = System.nanoTime();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + generatorName + "_table a JOIN " + generatorName + "_table b ON a.id = b.id");
        while (rs.next()) { // 결과 집합 반복하면서 모든 행 소비
        }
        return (System.nanoTime() - start) / 1_000_000; // 밀리 초로 변환
    }

    private static void createTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE UUID_TABLE (id VARCHAR(256) PRIMARY KEY)");
        stmt.execute("CREATE TABLE TIMEORDEREDUUID_TABLE (id VARCHAR(256) PRIMARY KEY)");
        stmt.execute("CREATE TABLE TIMEORDEREDEPOCHUUID_TABLE (id VARCHAR(256) PRIMARY KEY)");
        stmt.execute("CREATE TABLE ULID_TABLE (id VARCHAR(256) PRIMARY KEY)");
        stmt.execute("CREATE TABLE KSUID_TABLE (id VARCHAR(256) PRIMARY KEY)");
        stmt.execute("CREATE TABLE TSID_TABLE (id VARCHAR(256) PRIMARY KEY)");
        stmt.execute("CREATE TABLE TSIDLONG_TABLE (id BIGINT PRIMARY KEY)");
    }

    private static void dropTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE UUID_TABLE");
        stmt.execute("DROP TABLE TIMEORDEREDUUID_TABLE");
        stmt.execute("DROP TABLE TIMEORDEREDEPOCHUUID_TABLE");
        stmt.execute("DROP TABLE ULID_TABLE");
        stmt.execute("DROP TABLE KSUID_TABLE");
        stmt.execute("DROP TABLE TSID_TABLE");
        stmt.execute("DROP TABLE TSIDLONG_TABLE");
    }
}
