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

import java.util.Optional;

import org.mycore.lookup.backend.index.IndexManager;
import org.mycore.lookup.common.event.Event;
import org.mycore.lookup.common.event.Listener;
import org.mycore.lookup.common.event.annotation.EventListener;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@EventListener
public class IndexEventListener implements Listener {

    public static final String EVENT_INDEX = "index";

    private final IndexManager idx;

    public IndexEventListener() {
        idx = IndexManager.instance();
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.common.event.Listener#handleEvent(org.mycore.lookup.common.event.Event)
     */
    @Override
    public <T> void handleEvent(Event<T> event) throws Exception {
        if (event != null && EVENT_INDEX.equals(event.getType())) {
            Optional.ofNullable(event.getObject()).ifPresent(idx::set);
        }
    }

}
