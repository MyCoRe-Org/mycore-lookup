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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.MappedIdentifiers;
import org.mycore.lookup.api.service.LookupService;
import org.mycore.lookup.api.service.LookupService.Type;
import org.mycore.lookup.common.event.Event;
import org.mycore.lookup.common.event.EventManager;
import org.mycore.lookup.common.event.Listener;
import org.mycore.lookup.common.event.annotation.EventListener;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@EventListener
public class LookupEventListener implements Listener {

    public static final String EVENT_MAPIDS = "mapIds";

    public static final String EVENT_LOOKUP = "lookup";

    private static final Logger LOGGER = LogManager.getLogger();

    /* (non-Javadoc)
     * @see org.mycore.lookup.common.event.Listener#handleEvent(org.mycore.lookup.common.event.Event)
     */
    @Override
    public <T> void handleEvent(Event<T> event) throws Exception {
        if (event != null) {
            if (EVENT_LOOKUP.equals(event.getType())) {
                handleLookup(event.getObject());
            } else if (EVENT_MAPIDS.equals(event.getType())) {
                handleMapIds(event.getObject());
            }
        }
    }

    private <T> void handleLookup(T obj) {
        Optional.ofNullable(obj)
            .ifPresent(o -> EventManager.instance().fireEvent(new Event<T>(IndexEventListener.EVENT_INDEX, o)));
    }

    @SuppressWarnings("unchecked")
    private <T> void handleMapIds(T obj) {
        Optional.ofNullable(obj).map(o -> ((MappedIdentifiers<T>) o)).ifPresent(o -> {
            List<IdType> l = o.getMappedIds().parallelStream()
                .filter(idType -> Optional.ofNullable(LookupService.lookup(Type.fromValue(obj.getClass()), idType))
                    .isPresent())
                .distinct()
                .collect(Collectors.toList());

            if (!l.isEmpty()) {
                LOGGER.debug("mappedIds: {}", l);
            }
        });
    }

}
