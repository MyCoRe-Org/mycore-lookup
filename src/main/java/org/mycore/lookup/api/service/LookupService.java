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
package org.mycore.lookup.api.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.lookup.Application;
import org.mycore.lookup.api.entity.Corporate;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.MappedIdentifiers;
import org.mycore.lookup.api.entity.Person;
import org.mycore.lookup.api.entity.Place;
import org.mycore.lookup.api.entity.Scheme;
import org.mycore.lookup.api.service.annotation.Service;
import org.mycore.lookup.common.event.Event;
import org.mycore.lookup.common.event.EventManager;
import org.reflections.Reflections;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public abstract class LookupService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<? extends LookupService> SERVICES;

    protected List<Scheme> supportedSchemes;

    protected LookupService() {
        supportedSchemes = new ArrayList<>();
    }

    static {
        final Reflections reflections = new Reflections(Application.class.getPackage().getName());
        SERVICES = reflections.getTypesAnnotatedWith(Service.class).stream()
            .filter(cls -> LookupService.class.equals(cls.getAnnotation(Service.class).type()))
            .peek(cls -> LOGGER.info("register \"{}\"", cls.getName()))
            .map(cls -> {
                try {
                    return (LookupService) cls.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw (RuntimeException) e.getCause();
                }
            }).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <V extends MappedIdentifiers<V>> List<V> suggest(final Type type, final String term) {
        return merge(
            SERVICES.parallelStream()
                .collect(Collectors.toMap(svc -> svc.getClass(), svc -> {
                    try {
                        Method method = svc.getClass().getDeclaredMethod("suggest" + type.value().getSimpleName(),
                            String.class);
                        return (List<V>) Optional.ofNullable(method.invoke(svc, term))
                            .orElse(Collections.<V> emptyList());
                    } catch (NoSuchMethodException | SecurityException e) {
                        throw (RuntimeException) e.getCause();
                    } catch (IllegalAccessException e) {
                        throw (RuntimeException) e.getCause();
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof UnsupportedOperationException) {
                            return Collections.<V> emptyList();
                        } else if (e.getCause() instanceof SocketException) {
                            LOGGER.warn(e.getMessage(), e);
                            return Collections.<V> emptyList();
                        }
                        throw (RuntimeException) e.getCause();
                    }
                })).entrySet().stream()
                .sorted((ls1, ls2) -> {
                    return Integer.compare(
                        ls2.getKey().isAnnotationPresent(Priority.class)
                            ? ls2.getKey().getAnnotation(Priority.class).value() : 0,
                        ls1.getKey().isAnnotationPresent(Priority.class)
                            ? ls1.getKey().getAnnotation(Priority.class).value() : 0);
                })
                .filter(e -> !e.getValue().isEmpty())
                .flatMap(e -> e.getValue().stream())
                //                .peek(o -> EventManager.instance().fireEvent(new Event<V>("index", o)))
                .collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public static <V extends MappedIdentifiers<V>> V lookup(final Type type, final String id) {
        return Optional.ofNullable(IdType.parse(id)).map(idType -> (V) lookup(type, idType)).orElse(null);
    }

    public static <V extends MappedIdentifiers<V>> V lookup(final Type type, final Scheme scheme, final String id) {
        return lookup(type, new IdType(scheme, id));
    }

    @SuppressWarnings("unchecked")
    public static <V extends MappedIdentifiers<V>> V lookup(final Type type, final IdType idType) {
        V obj = merge(SERVICES.parallelStream().filter(svc -> svc.supportedSchemes.contains(idType.getScheme()))
            .collect(Collectors.toMap(svc -> svc.getClass(), svc -> {
                try {
                    Method method = svc.getClass().getDeclaredMethod(
                        type.value().getSimpleName().toLowerCase(Locale.ROOT), IdType.class);
                    return Optional.<V> ofNullable((V) method.invoke(svc, idType));
                } catch (NoSuchMethodException | SecurityException e) {
                    throw (RuntimeException) e.getCause();
                } catch (IllegalAccessException e) {
                    throw (RuntimeException) e.getCause();
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof UnsupportedOperationException) {
                        return Optional.<V> empty();
                    } else if (e.getCause() instanceof SocketException) {
                        LOGGER.warn(e.getMessage(), e);
                        return Optional.<V> empty();
                    }
                    throw (RuntimeException) e.getCause();
                }
            })).entrySet().stream()
            .sorted((ls1, ls2) -> {
                return Integer.compare(
                    ls2.getKey().isAnnotationPresent(Priority.class)
                        ? ls2.getKey().getAnnotation(Priority.class).value() : 0,
                    ls1.getKey().isAnnotationPresent(Priority.class)
                        ? ls1.getKey().getAnnotation(Priority.class).value() : 0);
            }).map(e -> e.getValue().orElse(null)).filter(e -> e != null).collect(Collectors.toList()))
                .stream()
                .findFirst().orElse(null);

        Optional.ofNullable(obj).ifPresent(o -> EventManager.instance().fireEvent(new Event<V>("index", o)));

        return obj;
    }

    private static <V> boolean anyMatch(List<V> il1, List<V> il2) {
        return ((BiFunction<List<V>, List<V>, Boolean>) (l1, l2) -> {
            return l1.stream().anyMatch(l2::contains);
        }).apply(il1, il2);
    }

    private static <V extends MappedIdentifiers<V>> List<V> merge(List<V> objs) {
        long startTime = System.currentTimeMillis();

        List<V> merged = new ArrayList<>();
        AtomicInteger numMerged = new AtomicInteger(0);

        objs.forEach(o -> {
            Optional<V> o2m = merged.stream()
                .filter(a -> anyMatch(o.getMappedIds(), a.getMappedIds()))
                .findFirst();
            if (o2m.isPresent()) {
                o2m.get().merge(o);
                numMerged.incrementAndGet();
            } else {
                merged.add(o);
            }
        });

        LOGGER.info("Merged {} in list of {}. ({}ms)", numMerged.get(),
            Optional.ofNullable(objs.size() == 0 ? objs : (Object) objs.get(0)).orElse(Object.class).getClass()
                .getSimpleName(),
            System.currentTimeMillis() - startTime);

        return merged;
    }

    public abstract List<Corporate> suggestCorporate(final String term) throws UnsupportedOperationException;

    public abstract List<Person> suggestPerson(final String term) throws UnsupportedOperationException;

    public abstract List<Place> suggestPlace(final String term) throws UnsupportedOperationException;

    public abstract Corporate corporate(IdType idType) throws UnsupportedOperationException;

    public abstract Person person(IdType idType) throws UnsupportedOperationException;

    public abstract Place place(IdType idType) throws UnsupportedOperationException;

    public static enum Type {
        PERSON(Person.class),

        CORPORATE(Corporate.class),

        PLACE(Place.class);

        private final Class<?> value;

        Type(final Class<?> value) {
            this.value = value;
        }

        public Class<?> value() {
            return value;
        }

        public static Type fromValue(final Class<?> value) {
            for (Type t : Type.values()) {
                if (t.value.equals(value)) {
                    return t;
                }
            }
            throw new IllegalArgumentException(value.getName());
        }
    }
}
