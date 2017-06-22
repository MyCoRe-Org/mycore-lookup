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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class IndexWriteAction implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private IndexWriteExecutor executor;

    private Term term;

    private Document doc;

    private boolean add = false;

    private boolean optimize = false;

    public static IndexWriteAction addAction(IndexWriteExecutor executor, Document doc) {
        IndexWriteAction a = new IndexWriteAction(executor);
        a.add = true;
        a.doc = doc;
        return a;
    }

    public static IndexWriteAction updateAction(IndexWriteExecutor executor, Term term, Document doc) {
        IndexWriteAction a = new IndexWriteAction(executor);
        a.add = false;
        a.term = term;
        a.doc = doc;
        return a;
    }

    public static IndexWriteAction optimizeAction(IndexWriteExecutor executor) {
        IndexWriteAction a = new IndexWriteAction(executor);
        a.optimize = true;
        return a;
    }

    private IndexWriteAction(IndexWriteExecutor executor) {
        this.executor = executor;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            if (add) {
                addDocument();
            } else if (optimize) {
                optimize();
            } else {
                updateDocument();
            }
        } catch (Exception e) {
            LOGGER.error("Error while writing Index ", e);
        }
    }

    private void addDocument() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("add Document:" + toString());
        }
        executor.getIndexWriter().addDocument(doc);
        executor.getIndexWriter().commit();
        LOGGER.debug("adding done.");
    }

    private void updateDocument() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("update Document:" + toString());
        }
        executor.getIndexWriter().updateDocument(term, doc);
        executor.getIndexWriter().commit();
        LOGGER.debug("updating done.");
    }

    private void optimize() throws IOException {
        LOGGER.debug("Optimize index...");
        executor.getIndexWriter().forceMerge(1, true);
        executor.getIndexWriter().forceMergeDeletes(true);
        executor.getIndexWriter().flush();
        executor.getIndexWriter().deleteUnusedFiles();
        executor.getIndexWriter().commit();
        LOGGER.debug("optimizing done.");
    }
}
