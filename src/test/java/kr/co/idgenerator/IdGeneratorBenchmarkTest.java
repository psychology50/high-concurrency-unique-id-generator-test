package kr.co.idgenerator;

import kr.co.idgenerator.strategy.IdGenerator;
import kr.co.idgenerator.strategy.KsuidGenerator;
import kr.co.idgenerator.strategy.TsidGenerator;
import kr.co.idgenerator.strategy.UuidGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    private <E extends Comparable<? super E>> void runBenchmark(IdGenerator<E> generator, String idType, int sampleSize) throws Exception {
        BenchmarkResult result = new BenchmarkResult(idType, sampleSize);
        result.setGenerationTime(testGenerationTime(generator, sampleSize));
        result.setSortable(testSortability(generator, sampleSize));
        result.setCollisionRate(testCollisionRate(generator, sampleSize));
        result.setDbJoinTime(testDbJoinPerformance(generator, idType, sampleSize));
        results.computeIfAbsent(idType, k -> new ArrayList<>()).add(result);
    }

    private <E> long testGenerationTime(IdGenerator<E> generator, int sampleSize) throws Exception {
        long start = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 0; i < sampleSize; i++) {
            executor.submit(generator::execute);
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        return (System.nanoTime() - start) / 1_000_000; // Convert to milliseconds
    }

    private <E extends Comparable<? super E>> boolean testSortability(IdGenerator<E> generator, int sampleSize) {
        List<E> ids = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            ids.add(generator.execute());
        }
        List<E> sortedIds = new ArrayList<>(ids);
        Collections.sort(sortedIds);
        return ids.equals(sortedIds);
    }

    private <E> double testCollisionRate(IdGenerator<E> generator, int sampleSize) throws Exception {
        Set<E> uniqueIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 0; i < sampleSize; i++) {
            executor.submit(() -> uniqueIds.add(generator.execute()));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        return 1 - ((double) uniqueIds.size() / sampleSize);
    }

    private <E> long testDbJoinPerformance(IdGenerator<E> generator, String idType, int sampleSize) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO " + idType + "_table (id) VALUES (?)");
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
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + idType + "_table a JOIN " + idType + "_table b ON a.id = b.id");
        while (rs.next()) { // Just iterate through the results
        }
        return (System.nanoTime() - start) / 1_000_000; // Convert to milliseconds
    }

    private static void createTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE UUID_TABLE (id VARCHAR(256) PRIMARY KEY)");
        stmt.execute("CREATE TABLE ULID_TABLE (id VARCHAR(256) PRIMARY KEY)");
        stmt.execute("CREATE TABLE KSUID_TABLE (id VARCHAR(256) PRIMARY KEY)");
    }

    private static void dropTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE UUID_TABLE");
        stmt.execute("DROP TABLE ULID_TABLE");
        stmt.execute("DROP TABLE KSUID_TABLE");
    }
}
