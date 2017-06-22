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
package org.mycore.lookup.common.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.lookup.Application;
import org.mycore.lookup.common.event.annotation.EventListener;
import org.reflections.Reflections;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class EventManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private static EventManager INSTANCE = null;

    private final Map<String, Listener> listeners = new ConcurrentHashMap<>();

    private final ThreadPoolExecutor executor;

    public static EventManager instance() {
        if (INSTANCE == null) {
            synchronized (EventManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EventManager();
                }
            }
        }

        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    private EventManager() {
        final Reflections reflections = new Reflections(Application.class.getPackage().getName());
        reflections.getTypesAnnotatedWith(EventListener.class)
            .forEach(el -> addListener((Class<? extends Listener>) el));

        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public void addListener(final Class<? extends Listener> clazz) {
        try {
            addListener(clazz, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void addListener(final Listener listener) {
        addListener(listener.getClass(), listener);
    }

    public void addListener(final Class<? extends Listener> clazz, final Listener listener) {
        LOGGER.info("register \"{}\"", clazz.getName());
        listeners.put(clazz.getName(), listener);
    }

    public void removeListner(final Listener listener) {
        removeListner(listener.getClass(), listener);
    }

    public void removeListner(final Class<? extends Listener> clazz, final Listener listener) {
        LOGGER.info("unregister \"{}\"", clazz.getName());
        listeners.remove(listener.getClass().getName());
    }

    public void fireEvent(final Event<?> event) {
        listeners.values().forEach(d -> {
            try {
                d.handleEvent(event);
            } catch (Exception ex) {
                throw new UnsupportedOperationException(ex);
            }
        });
    }

    public void fireEvent(final Class<? extends Listener> delegate, final Event<?> event) {
        listeners.entrySet().stream().filter(e -> e.getKey().equals(delegate.getName())).findFirst().ifPresent(e -> {
            try {
                e.getValue().handleEvent(event);
            } catch (Exception ex) {
                throw new UnsupportedOperationException(ex);
            }
        });
    }

    public void fireAsyncEvent(final Event<?> event) {
        listeners.values().forEach(d -> {
            executor.submit(() -> {
                try {
                    d.handleEvent(event);
                } catch (Exception ex) {
                    throw new UnsupportedOperationException(ex);
                }
            });
        });
    }

    public void fireAsyncEvent(final Class<? extends Listener> delegate, final Event<?> event) {
        listeners.entrySet().stream().filter(e -> e.getKey().equals(delegate.getName())).findFirst().ifPresent(e -> {
            executor.submit(() -> {
                try {
                    e.getValue().handleEvent(event);
                } catch (Exception ex) {
                    throw new UnsupportedOperationException(ex);
                }
            });
        });
    }
}
