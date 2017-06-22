/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */
package org.mycore.lookup.backend.index;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.mycore.lookup.common.config.Configuration;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class IndexWriteExecutor extends ThreadPoolExecutor {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Configuration CONFIG = Configuration.instance();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Runnable delayedCloser = () -> {
        if (!this.writerClosed && this.getQueue().isEmpty()) {
            this.closeIndexWriter();
        }
    };

    private boolean writerClosed, closeWriterEarly;

    private IndexWriter indexWriter;

    private Directory indexDir;

    private ScheduledFuture<?> delayedFuture;

    private int maxIndexWriteActions;

    private ReadWriteLock indexCloserLock = new ReentrantReadWriteLock(true);

    private ThreadLocal<Lock> writeAccess = ThreadLocal.withInitial(() -> indexCloserLock.readLock());

    private static IndexWriter getWriter(Directory indexDir) throws Exception {
        IndexWriter w;
        Analyzer analyzer = CONFIG.getInstanceOf("Index.Analyzer", new StandardAnalyzer());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setMaxBufferedDocs(2000);
        w = new IndexWriter(indexDir, config);
        return w;
    }

    public IndexWriteExecutor(BlockingQueue<Runnable> workQueue, Directory indexDir) {
        super(1, 1, 0, TimeUnit.SECONDS, workQueue);
        this.indexDir = indexDir;
        writerClosed = true;
        closeWriterEarly = CONFIG.getBoolean("Index.closeWriterEarly", false);
        maxIndexWriteActions = CONFIG.getInt("Index.maxIndexWriteActions", 500);
    }

    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    private void openIndexWriter() {
        try {
            LOGGER.debug("Opening Lucene index for writing.");
            if (indexWriter == null) {
                indexWriter = getWriter(indexDir);
            }
        } catch (Exception e) {
            LOGGER.warn("Error while reopening IndexWriter.", e);
        } finally {
            writerClosed = false;
        }
    }

    private void closeIndexWriter() {
        Lock writerLock = indexCloserLock.writeLock();
        try {
            writerLock.lock();
            if (indexWriter != null) {
                LOGGER.debug("Writing Lucene index changes to disk.");
                indexWriter.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Error while closing IndexWriter.", e);
        } catch (IllegalStateException e) {
            LOGGER.debug("IndexWriter was allready closed.", e);
        } finally {
            writerClosed = true;
            indexWriter = null;
            writerLock.unlock();
        }
    }

    private void cancelDelayedIndexCloser() {
        if (delayedFuture != null && !delayedFuture.isDone()) {
            delayedFuture.cancel(false);
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread, java.lang.Runnable)
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        cancelDelayedIndexCloser();
        writeAccess.get().lock();
        if (writerClosed) {
            openIndexWriter();
        }
        super.beforeExecute(t, r);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        writeAccess.get().unlock();
        if (closeWriterEarly || getCompletedTaskCount() % maxIndexWriteActions == 0) {
            closeIndexWriter();
        } else {
            cancelDelayedIndexCloser();
            try {
                delayedFuture = scheduler.schedule(delayedCloser, 2, TimeUnit.SECONDS);
            } catch (RejectedExecutionException e) {
                LOGGER.warn("Cannot schedule delayed IndexWriter closer. Closing IndexWriter now.");
                closeIndexWriter();
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadPoolExecutor#shutdown()
     */
    @Override
    public void shutdown() {
        cancelDelayedIndexCloser();
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(60 * 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn("Error while closing DelayedIndexWriterCloser", e);
        }
        super.shutdown();
        closeIndexWriter();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadPoolExecutor#finalize()
     */
    @Override
    protected void finalize() {
        closeIndexWriter();
        super.finalize();
    }
}
