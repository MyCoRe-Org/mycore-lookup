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
package org.mycore.lookup.api.event;

import org.mycore.lookup.backend.index.IndexManager;
import org.mycore.lookup.common.event.Event;
import org.mycore.lookup.common.event.EventManager;
import org.mycore.lookup.common.event.Listener;
import org.mycore.lookup.common.event.annotation.AutoExecutable;
import org.mycore.lookup.common.event.annotation.Startup;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@AutoExecutable(name = "IndexEventListener")
public class IndexEventListener implements Listener {

    @Startup
    protected static void startUp() {
        EventManager.instance().addListener(new IndexEventListener());
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.common.event.Listener#handleEvent(org.mycore.lookup.common.event.Event)
     */
    @Override
    public <T> void handleEvent(Event<T> event) throws Exception {
        if (event != null && event.getObject() != null) {
            IndexManager.instance().set(event.getObject());
        }
    }

}