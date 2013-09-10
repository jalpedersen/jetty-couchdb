//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
/**
 * Based on org.eclipse.jetty.util.thread.ExecutorThreadPool
 * 
 * Modified by Jesper André Lyngesen Pedersen (jalpedersen@gmail.com), 2013
 * 
 */

package org.signaut.util.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ThreadPool;

/* ------------------------------------------------------------ */
/**
 * Jetty ThreadPool using java 5 ThreadPoolExecutor
 * This class wraps a {@link ExecutorService} as a {@link ThreadPool} and
 * {@link LifeCycle} interfaces so that it may be used by the Jetty <code>org.eclipse.jetty.server.Server</code>
 */
public class ReplaceableThreadPool extends AbstractLifeCycle implements ThreadPool, LifeCycle
{
    private static final Logger LOG = Log.getLogger(ReplaceableThreadPool.class);
    private ExecutorService replaceableExecutor;
	private final int corePoolSize;
	private final int maximumPoolSize;
	private final long keepAliveTime;
	private final TimeUnit unit;
	private final BlockingQueue<Runnable> workQueue;

  
	  public ReplaceableThreadPool(int corePoolSize)
	    {
	        this(corePoolSize, corePoolSize, 30*1000, TimeUnit.MILLISECONDS);
	    }
 
    /* ------------------------------------------------------------ */
    /**
     * Wraps an {@link ThreadPoolExecutor} using
     * an unbounded {@link LinkedBlockingQueue} is used for the jobs queue;
     * @param corePoolSize must be equal to maximumPoolSize
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime the max time a thread can remain idle, in milliseconds
     */
    public ReplaceableThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime)
    {
        this(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS);
    }

    /* ------------------------------------------------------------ */
    /**
     * Wraps an {@link ThreadPoolExecutor} using
     * an unbounded {@link LinkedBlockingQueue} is used for the jobs queue.
     * @param corePoolSize must be equal to maximumPoolSize
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime the max time a thread can remain idle
     * @param unit the unit for the keepAliveTime
     */
    public ReplaceableThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit)
    {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>());
    }

    /* ------------------------------------------------------------ */

    /**
     * Wraps an {@link ThreadPoolExecutor}
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime the max time a thread can remain idle
     * @param unit the unit for the keepAliveTime
     * @param workQueue the queue to use for holding tasks before they are executed
     */
    public ReplaceableThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
    {
    	this.corePoolSize = corePoolSize;
    	this.maximumPoolSize = maximumPoolSize;
    	this.keepAliveTime = keepAliveTime;
    	this.unit = unit;
    	this.workQueue = workQueue;
        this.replaceableExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }


    /* ------------------------------------------------------------ */
    @Override
    public void execute(Runnable job)
    {
        replaceableExecutor.execute(job);
    }

    /* ------------------------------------------------------------ */
    public boolean dispatch(Runnable job)
    {
        try
        {
            replaceableExecutor.execute(job);
            return true;
        }
        catch(RejectedExecutionException e)
        {
            LOG.warn(e);
            return false;
        }
    }

    /* ------------------------------------------------------------ */
    public int getIdleThreads()
    {
        if (replaceableExecutor instanceof ThreadPoolExecutor)
        {
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)replaceableExecutor;
            return tpe.getPoolSize() - tpe.getActiveCount();
        }
        return -1;
    }

    /* ------------------------------------------------------------ */
    public int getThreads()
    {
        if (replaceableExecutor instanceof ThreadPoolExecutor)
        {
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)replaceableExecutor;
            return tpe.getPoolSize();
        }
        return -1;
    }

    /* ------------------------------------------------------------ */
    public boolean isLowOnThreads()
    {
        if (replaceableExecutor instanceof ThreadPoolExecutor)
        {
            final ThreadPoolExecutor tpe = (ThreadPoolExecutor)replaceableExecutor;
            // getActiveCount() locks the thread pool, so execute it last
            return tpe.getPoolSize() == tpe.getMaximumPoolSize() &&
                    tpe.getQueue().size() >= tpe.getPoolSize() - tpe.getActiveCount();
        }
        return false;
    }

    /* ------------------------------------------------------------ */
    public void join() throws InterruptedException
    {
        replaceableExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public synchronized void replaceThreads() {
        final ExecutorService obsolete = replaceableExecutor;
    	this.replaceableExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        LOG.debug("Replacing threads");
    	obsolete.shutdown();
    }
    
    /* ------------------------------------------------------------ */
    @Override
    protected void doStop() throws Exception
    {
        super.doStop();
        replaceableExecutor.shutdownNow();
    }
}