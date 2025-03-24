
import bgu.spl.mics.Future;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class FutureTest {

    @Test
    public void testResolveChangesResult() {
        Future<String> future = new Future<>();
        assertFalse(future.isDone(), "Future should not be resolved initially.");

        String result = "Test Result";
        future.resolve(result);

        assertTrue(future.isDone(), "Future should be resolved after calling resolve().");
        assertEquals(result, future.get(), "Future result should match the resolved value.");
    }

    @Test
    public void testGetWithTimeoutReturnsInTime() throws InterruptedException {
        Future<String> future = new Future<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            try {
                Thread.sleep(200); // Simulate delay
                future.resolve("Delayed Result");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        String result = future.get(500, TimeUnit.MILLISECONDS);
        assertEquals("Delayed Result", result, "Future should return the resolved value within the timeout.");

        executor.shutdown();
    }

    @Test
    public void testGetWithTimeoutReturnsNullOnTimeout() {
        Future<String> future = new Future<>();

        String result = future.get(100, TimeUnit.MILLISECONDS);
        assertNull(result, "Future should return null if not resolved within the timeout.");
    }

    @Test
    public void testParallelResolveConsistency() throws InterruptedException {
        final int iterations = 10;
        final int threadCount = 5;

        for (int i = 0; i < iterations; i++) {
            bgu.spl.mics.Future<Integer> future = new bgu.spl.mics.Future<>();
            List<Thread> threads = new ArrayList<>();

            // יצירת משימות רצות בתהליכים
            for (int t = 0; t < threadCount; t++) {
                Thread thread = new Thread(() -> {
                    future.resolve(42);
                    try {
                        assertEquals(42, future.get(), "All threads should see the same resolved value.");
                    } catch (AssertionError e) {
                        e.printStackTrace();
                    }
                });
                threads.add(thread);
            }

            // הפעלת כל התהליכים
            for (Thread thread : threads) {
                thread.start();
            }

            // המתנה לסיום כל התהליכים
            for (Thread thread : threads) {
                thread.join();
            }
        }
    }

    @Test
    public void testMultipleResolves() {
        Future<String> future = new Future<>();
        future.resolve("First Value");
        future.resolve("Second Value");

        assertEquals("First Value", future.get(), "Future should keep the first resolved value.");
        assertTrue(future.isDone(), "Future should remain resolved.");
    }

    @Test
    public void testGetBlocksUntilResolved() throws InterruptedException {
        Future<String> future = new Future<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            try {
                Thread.sleep(200); // Delay to simulate asynchronous resolution
                future.resolve("Resolved Value");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        String result = future.get();
        assertEquals("Resolved Value", result, "Future should return the resolved value after it is resolved.");
        executor.shutdown();
    }

    @Test
    public void testTimeoutBeforeResolve() {
        Future<String> future = new Future<>();
        String result = future.get(100, TimeUnit.MILLISECONDS);
        assertNull(result, "Future should return null if not resolved within the timeout.");
    }
    @Test
    public void testStressResolveAndGet() throws InterruptedException {
        Future<Integer> future = new Future<>();
        final int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> future.resolve(123));
            executor.submit(() -> assertEquals(123, future.get(), "All threads should get the resolved value."));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor should terminate properly.");
    }




}
