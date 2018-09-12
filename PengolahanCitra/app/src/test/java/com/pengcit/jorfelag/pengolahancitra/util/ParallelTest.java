package com.pengcit.jorfelag.pengolahancitra.util;

import org.junit.Test;

/**
 * https://stackoverflow.com/a/5849282
 */
public class ParallelTest {
    private int k;

    @Test
    public void testFor() {
        k = 0;
        Parallel.For(0, 10, new LoopBody<Integer>() {
            public void run(Integer i) {
                k += i;
                System.out.println(i);
            }
        });
        System.out.println("Sum = " + k);
    }
}