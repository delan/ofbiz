/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.base.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;

import org.ofbiz.base.lang.SourceMonitored;
import org.ofbiz.base.util.Debug;

@SourceMonitored
public final class ExecutionPool {
    public static final String module = ExecutionPool.class.getName();
    public static final ExecutorService GLOBAL_BATCH = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ExecutionPoolThreadFactory(null, "OFBiz-batch"));
    public static final ForkJoinPool GLOBAL_FORK_JOIN = new ForkJoinPool();
    private static final ExecutorService pulseExecutionPool = Executors.newFixedThreadPool(autoAdjustThreadCount(-1), new ExecutionPoolThreadFactory(null, "OFBiz-ExecutionPoolPulseWorker"));

    protected static class ExecutionPoolThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final String namePrefix;
        private volatile int count = 1;

        protected ExecutionPoolThreadFactory(ThreadGroup group, String namePrefix) {
            this.group = group;
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r);
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            t.setName(namePrefix + "-" + count++);
            return t;
        }
    }

    private static int autoAdjustThreadCount(int threadCount) {
        if (threadCount == 0) {
            return 1;
        } else if (threadCount < 0) {
            int numCpus = Runtime.getRuntime().availableProcessors();
            return Math.abs(threadCount) * numCpus;
        } else {
            return threadCount;
        }
    }

    public static ScheduledExecutorService getScheduledExecutor(ThreadGroup group, String namePrefix, int threadCount, boolean preStart) {
        return getScheduledExecutor(group, namePrefix, threadCount, 0, preStart);
    }
    public static ScheduledExecutorService getScheduledExecutor(ThreadGroup group, String namePrefix, int threadCount, long keepAliveSeconds, boolean preStart) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(autoAdjustThreadCount(threadCount), new ExecutionPoolThreadFactory(group, namePrefix));
        if (keepAliveSeconds > 0) {
            executor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
            executor.allowCoreThreadTimeOut(true);
        }
        if (preStart) {
            executor.prestartAllCoreThreads();
        }
        return executor;
    }

    public static <F> List<F> getAllFutures(Collection<Future<F>> futureList) {
        List<F> result = FastList.newInstance();
        for (Future<F> future: futureList) {
            try {
                result.add(future.get());
            } catch (ExecutionException e) {
                Debug.logError(e, module);
            } catch (InterruptedException e) {
                Debug.logError(e, module);
            }
        }
        return result;
    }

    public static void addPulse(Pulse pulse) {
        delayQueue.put(pulse);
    }

    public static void removePulse(Pulse pulse) {
        delayQueue.remove(pulse);
    }

    static {
        int numberOfExecutionPoolPulseWorkers = autoAdjustThreadCount(-1);
        for (int i = 0; i < numberOfExecutionPoolPulseWorkers; i++) {
            pulseExecutionPool.execute(new ExecutionPoolPulseWorker());
        }
    }

    private static final DelayQueue<Pulse> delayQueue = new DelayQueue<Pulse>();

    public static class ExecutionPoolPulseWorker implements Runnable {
        public void run() {
            try {
                while (true) {
                    delayQueue.take().run();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static abstract class Pulse implements Delayed, Runnable {
        protected final long expireTimeNanos;
        protected final long loadTimeNanos;

        protected Pulse(long delayNanos) {
            this(System.nanoTime(), delayNanos);
        }

        protected Pulse(long loadTimeNanos, long delayNanos) {
            this.loadTimeNanos = loadTimeNanos;
            expireTimeNanos = loadTimeNanos + delayNanos;
        }

        public long getLoadTimeNanos() {
            return loadTimeNanos;
        }

        public long getExpireTimeNanos() {
            return expireTimeNanos;
        }

        public final long getDelay(TimeUnit unit) {
            return unit.convert(expireTimeNanos - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        public final int compareTo(Delayed other) {
            long r = (expireTimeNanos - ((Pulse) other).expireTimeNanos);
            if (r < 0) return -1;
            if (r > 0) return 1;
            return 0;
        }
    }

}
