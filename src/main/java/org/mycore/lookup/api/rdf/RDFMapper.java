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
package org.mycore.lookup.api.rdf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.util.FileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Scheme;
import org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter;
import org.mycore.lookup.api.rdf.annotation.RDFMapping;
import org.mycore.lookup.api.rdf.annotation.RDFMapping.RDFMappings;
import org.mycore.lookup.util.ObjectTools;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class RDFMapper {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final FileManager FILE_MGR = FileManager.get();

    private static final Map<Class<?>, Map<Method, RDFMapping[]>> METHOD_CACHE = new HashMap<>();

    static {
        FILE_MGR.setModelCaching(true);
    }

    /**
     * Maps RDF Triples to annotated object class.
     * 
     * @param uri the RDF URI
     * @param cls the annotated object
     * @return a RDF mapped object
     */
    public static <T> T map(URI uri, Class<T> cls) {
        try {
            final T obj = cls.newInstance();

            Model model = loadModel(uri);
            if (model != null) {
                model.listSubjects().forEachRemaining(res -> map(res, obj));
                return obj;
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw (RuntimeException) e.getCause();
        }

        return null;
    }

    private static Model loadModel(URI uri) {
        IdType idType = Scheme.extractIdTypeFromURI(uri);

        if (idType != null) {
            Scheme scheme = idType.getScheme();
            String id = idType.getId();
            URI rdfURI = scheme.buildRdfURI(id);

            try {
                Model model = FILE_MGR.loadModel(rdfURI.toString(), scheme.getRdfType());
                //                model.listStatements().forEachRemaining(LOGGER::info);
                //                model.write(System.out);
                return model;
            } catch (NotFoundException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Maps a RDF {@link Resource} to a object.
     * 
     * @param res the {@link Resource}
     * @param obj the object to map
     */
    @SuppressWarnings("unchecked")
    public static <T> void map(Resource res, T obj) {
        long startTime = System.currentTimeMillis();
        AtomicInteger numMapped = new AtomicInteger(0);

        Class<?> cls = obj.getClass();
        Model model = res.getModel();

        Map<Method, RDFMapping[]> mm = annotatedMethods(cls);

        mm.forEach((m, as) -> {
            Arrays.stream(as).forEach(a -> {
                Resource r = res;
                Property p = model.createProperty(a.uri());
                if (!a.nodeUri().isEmpty()) {
                    Property np = model.createProperty(a.nodeUri());
                    if (res.hasProperty(np)) {
                        r = r.getProperty(np).getResource();
                    } else {
                        r = null;
                    }
                }
                if (r != null && r.hasProperty(p)) {
                    Method setter = ObjectTools.getSetter(cls, m);
                    setter.setAccessible(true);
                    try {
                        if (Collection.class.isAssignableFrom(m.getReturnType())) {
                            Collection<?> values = r.listProperties(p).toList().stream()
                                .map(s -> nodeValue(s.getObject())).collect(Collectors.toList());
                            LOGGER.debug("map \"{}\" to {}", values, setter);
                            setter.invoke(obj, adapter(a).unmarshal(values));
                        } else {
                            Object value = nodeValue(r.getProperty(p).getObject());
                            LOGGER.debug("map \"{}\" to {}", value, setter);
                            setter.invoke(obj, adapter(a).unmarshal(value));
                        }
                        numMapped.incrementAndGet();
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException | InstantiationException e) {
                        throw (RuntimeException) e.getCause();
                    }
                }
            });
        });

        LOGGER.info("Mapped {} into {}. ({}ms)", numMapped, cls.getSimpleName(),
            System.currentTimeMillis() - startTime);
    }

    private static String nodeValue(RDFNode n) {
        return n.isLiteral() ? n.asLiteral().getString() : n.toString();
    }

    private static Map<Method, RDFMapping[]> annotatedMethods(Class<?> cls) {
        if (!METHOD_CACHE.containsKey(cls)) {
            METHOD_CACHE.put(cls, Arrays.stream(cls.getMethods())
                .filter(m -> m.isAnnotationPresent(RDFMappings.class) || m.isAnnotationPresent(RDFMapping.class))
                .collect(Collectors.toMap(m -> m,
                    m -> m.isAnnotationPresent(RDFMappings.class) ? m.getAnnotation(RDFMappings.class).value()
                        : new RDFMapping[] { m.getAnnotation(RDFMapping.class) })));
        }

        return METHOD_CACHE.get(cls);
    }

    @SuppressWarnings("rawtypes")
    private static RDFMappingAdapter adapter(RDFMapping a) throws NoSuchMethodException, SecurityException,
        InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<? extends RDFMappingAdapter> aCls = a.adapter();
        Constructor<?> constructor = aCls.getDeclaredConstructor();
        constructor.setAccessible(true);
        return (RDFMappingAdapter) constructor.newInstance();
    }

}
