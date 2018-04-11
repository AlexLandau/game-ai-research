package net.alloyggp.research.applications.debug;

import java.util.Arrays;

import org.hipparchus.random.RandomGenerator;

public final class PrintlningRandomGenerator implements RandomGenerator {
    private final RandomGenerator delegate;

    public PrintlningRandomGenerator(RandomGenerator delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setSeed(int seed) {
        System.out.println("setSeed(" + seed + ")");
        delegate.setSeed(seed);
    }

    @Override
    public void setSeed(int[] seed) {
        System.out.println("setSeed(" + Arrays.toString(seed) + ")");
        delegate.setSeed(seed);
    }

    @Override
    public void setSeed(long seed) {
        System.out.println("setSeed(" + seed + ")");
        delegate.setSeed(seed);
    }

    @Override
    public void nextBytes(byte[] bytes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Implement me");
    }

    @Override
    public void nextBytes(byte[] bytes, int offset, int len) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Implement me");
    }

    @Override
    public int nextInt() {
        int result = delegate.nextInt();
        System.out.println("nextInt() = " + result);
        return result;
    }

    @Override
    public int nextInt(int n) {
        int result = delegate.nextInt(n);
        System.out.println("nextInt(" + n + ") = " + result);
        return result;
    }

    @Override
    public long nextLong() {
        long result = delegate.nextLong();
        System.out.println("nextLong() = " + result);
        return result;
    }

    @Override
    public long nextLong(long n) {
        long result = delegate.nextLong(n);
        System.out.println("nextLong(" + n + ") = " + result);
        return result;
    }

    @Override
    public boolean nextBoolean() {
        boolean result = delegate.nextBoolean();
        System.out.println("nextBoolean() = " + result);
        return result;
    }

    @Override
    public float nextFloat() {
        float result = delegate.nextFloat();
        System.out.println("nextFloat() = " + result);
        return result;
    }

    @Override
    public double nextDouble() {
        double result = delegate.nextDouble();
        System.out.println("nextDouble() = " + result);
        return result;
    }

    @Override
    public double nextGaussian() {
        double result = delegate.nextGaussian();
        System.out.println("nextGaussian() = " + result);
        return result;
    }

}
