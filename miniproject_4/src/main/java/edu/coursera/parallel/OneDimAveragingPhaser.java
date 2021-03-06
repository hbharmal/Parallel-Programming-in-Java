package edu.coursera.parallel;

import java.util.concurrent.Phaser;

/**
 * Wrapper class for implementing one-dimensional iterative averaging using
 * phasers.
 */
public final class OneDimAveragingPhaser {
    /**
     * Default constructor.
     */
    private OneDimAveragingPhaser() {
    }

    /**
     * Sequential implementation of one-dimensional iterative averaging.
     *
     * @param iterations The number of iterations to run
     * @param myNew A double array that starts as the output array
     * @param myVal A double array that contains the initial input to the
     *        iterative averaging problem
     * @param n The size of this problem
     */
    public static void runSequential(final int iterations, final double[] myNew,
            final double[] myVal, final int n) {
        double[] next = myNew;
        double[] curr = myVal;

        for (int iter = 0; iter < iterations; iter++) {
            for (int j = 1; j <= n; j++) {
                next[j] = (curr[j - 1] + curr[j + 1]) / 2.0;
            }
            double[] tmp = curr;
            curr = next;
            next = tmp;
        }
    }

    /**
     * An example parallel implementation of one-dimensional iterative averaging
     * that uses phasers as a simple barrier (arriveAndAwaitAdvance).
     *
     * @param iterations The number of iterations to run
     * @param myNew A double array that starts as the output array
     * @param myVal A double array that contains the initial input to the
     *        iterative averaging problem
     * @param n The size of this problem
     * @param tasks The number of threads/tasks to use to compute the solution
     */
    public static void runParallelBarrier(final int iterations,
            final double[] myNew, final double[] myVal, final int n,
            final int tasks) {
        Phaser ph = new Phaser(0);
        ph.bulkRegister(tasks);

        Thread[] threads = new Thread[tasks];

        for (int ii = 0; ii < tasks; ii++) {
            final int i = ii;

            threads[ii] = new Thread(() -> {
                double[] threadPrivateMyVal = myVal;
                double[] threadPrivateMyNew = myNew;

                final int chunkSize = (n / tasks);
                final int left = i * chunkSize + 1;
                int right = left + chunkSize - 1;
                if (right > n) right = n;

                for (int iter = 0; iter < iterations; iter++) {
                    for (int j = left; j <= right; j++) {
                        threadPrivateMyNew[j] = (threadPrivateMyVal[j - 1]
                            + threadPrivateMyVal[j + 1]) / 2.0;
                    }
                    ph.arriveAndAwaitAdvance();

                    double[] temp = threadPrivateMyNew;
                    threadPrivateMyNew = threadPrivateMyVal;
                    threadPrivateMyVal = temp;
                }
            });
            threads[ii].start();
        }

        for (int ii = 0; ii < tasks; ii++) {
            try {
                threads[ii].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A parallel implementation of one-dimensional iterative averaging that
     * uses the Phaser.arrive and Phaser.awaitAdvance APIs to overlap
     * computation with barrier completion.
     *
     * TODO Complete this method based on the provided runSequential and
     * runParallelBarrier methods.
     *
     * @param iterations The number of iterations to run
     * @param myNew A double array that starts as the output array
     * @param myVal A double array that contains the initial input to the
     *              iterative averaging problem
     * @param n The size of this problem
     * @param tasks The number of threads/tasks to use to compute the solution
     */
    public static void runParallelFuzzyBarrier(final int iterations,
            final double[] myNew, final double[] myVal, final int n,
            final int tasks) {

        final int numCores = tasks;

        // Create phaser object
        Phaser phaser = new Phaser(0);
        phaser.bulkRegister(numCores);

        Thread[] threads = new Thread[numCores];

        // Initialize each thread
        for (int i = 0; i < threads.length; i++) {

            final int currentIndex = i;
            Runnable runnable = new Runnable() {

                @Override
                public void run() {

                    double[] localMyVal = myVal;
                    double[] localMyNew = myNew;

                    final int chunkSize = (n / tasks);

                    // Get and compute leftmost element of current thread
                    final int left = currentIndex * chunkSize + 1;
                    localMyNew[left] = (localMyVal[left - 1] + localMyVal[left + 1]) / 2.0;

                    // Get and compute rightmost element of current thread
                    int right = left + chunkSize - 1;
                    if (right > n) right = n;
                    localMyNew[right] = (localMyVal[right - 1] + localMyVal[right + 1]) / 2.0;

                    int currentPhase = phaser.arrive();

                    for (int j = 0; j < right - 1; j++) {
                        localMyNew[j] = (localMyVal[j - 1] + localMyVal[j + 1]) / 2.0;
                    }

                    // swap
                    double[] current = localMyNew;
                    localMyNew = localMyVal;
                    localMyVal = current;

                }
            };

            threads[i] = new Thread(runnable);
            threads[i].start();

        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.out.println(e.getStackTrace());
            }
        }

    }
}
