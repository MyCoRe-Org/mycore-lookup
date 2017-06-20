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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.mycore.lookup.backend.index.adapter.FieldAdapter;
import org.mycore.lookup.backend.index.annotation.Field;
import org.mycore.lookup.backend.index.annotation.Id;
import org.mycore.lookup.backend.index.annotation.IdRef;
import org.mycore.lookup.common.config.Configuration;
import org.mycore.lookup.common.config.ConfigurationDir;
import org.mycore.lookup.util.ObjectTools;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class IndexManager {

    private static final Configuration CONFIG = Configuration.instance();

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<Class<?>, Map<Method, Field>> FIELD_CACHE = new HashMap<>();

    private static final Map<Class<?>, Map<Method, IdRef>> REF_CACHE = new HashMap<>();

    private static final String OBJECT_CLASS_FIELD = "objectClass";

    private static final int DEFAULT_LIMIT = 10;

    private static IndexManager INSTANCE;

    private final StopwordAnalyzerBase analyzer;

    private final Directory index;

    public static IndexManager instance() {
        if (INSTANCE == null) {
            INSTANCE = new IndexManager();
        }
        return INSTANCE;
    }

    private IndexManager() {
        analyzer = CONFIG.getInstanceOf("Index.Analyzer", new StandardAnalyzer());
        try {
            String indexPath = CONFIG.getString("Index.Path",
                ConfigurationDir.getConfigurationDirectory().getAbsolutePath() + File.separator + "data"
                    + File.separator + "index");
            Path path = Paths.get(indexPath);
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }
            index = FSDirectory.open(path);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return the analyzer
     */
    public StopwordAnalyzerBase getAnalyzer() {
        return analyzer;
    }

    /**
     * @return the index
     */
    public Directory getIndex() {
        return index;
    }

    private Map<Method, Field> annotatedMethods(Class<?> cls) {
        if (!FIELD_CACHE.containsKey(cls)) {
            FIELD_CACHE.put(cls, Arrays.stream(cls.getMethods()).filter(m -> m.isAnnotationPresent(Field.class))
                .collect(Collectors.toMap(m -> m, m -> m.getAnnotation(Field.class))));
        }

        return FIELD_CACHE.get(cls);
    }

    private Map<Method, IdRef> annotatedRefs(Class<?> cls) {
        if (!REF_CACHE.containsKey(cls)) {
            REF_CACHE.put(cls, Arrays.stream(cls.getMethods()).filter(m -> m.isAnnotationPresent(IdRef.class))
                .collect(Collectors.toMap(m -> m, m -> m.getAnnotation(IdRef.class))));
        }

        return REF_CACHE.get(cls);
    }

    @SuppressWarnings("rawtypes")
    private FieldAdapter adapter(Field a) throws NoSuchMethodException, SecurityException,
        InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<? extends FieldAdapter> aCls = a.adapter();
        Constructor<?> constructor = aCls.getDeclaredConstructor();
        constructor.setAccessible(true);
        return (FieldAdapter) constructor.newInstance();
    }

    public <T> void set(T obj) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);

        saveOrUpdate(w, obj);
        buildReferences(obj).forEach(o -> {
            try {
                saveOrUpdate(w, o);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        w.commit();
        w.close();
    }

    public <T> void set(List<T> objs) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);

        objs.forEach(obj -> {
            try {
                saveOrUpdate(w, obj);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        w.commit();
        w.close();
    }

    private <T> void saveOrUpdate(IndexWriter w, T obj) throws IOException {
        if (!exists(obj)) {
            LOGGER.info("add {}", obj);
            w.addDocument(buildDocument(obj));
        } else {
            LOGGER.info("update {}", obj);
            w.updateDocument(buildIdTerm(obj), buildDocument(obj));
        }
    }

    private <T> Document buildDocument(T obj) {
        Class<?> objCls = obj.getClass();
        String fieldPrefix = objCls.getSimpleName() + ".";
        Map<Method, Field> mm = annotatedMethods(objCls);

        Document doc = new Document();
        doc.add(new StringField(OBJECT_CLASS_FIELD, objCls.getName(), Store.YES));

        mm.forEach((m, f) -> {
            String name = fieldPrefix + (f.name().isEmpty() ? ObjectTools.getFieldName(m.getName()) : f.name());
            Method getter = ObjectTools.getGetter(objCls, m);

            try {
                if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                    Optional.ofNullable((Collection<?>) getter.invoke(obj))
                        .ifPresent(vals -> {
                            LOGGER.debug("set {}=\"{}\" from {}", name, vals, getter);
                            vals.forEach(v -> {
                                Optional.ofNullable(marshal(f, v))
                                    .ifPresent(value -> doc.add(buildIndexableField(f, name, value)));
                            });
                        });
                } else {
                    Optional.ofNullable(marshal(f, getter.invoke(obj))).ifPresent(value -> {
                        LOGGER.debug("set {}=\"{}\" from {}", name, value, getter);
                        doc.add(buildIndexableField(f, name, value));
                    });
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
                throw (RuntimeException) e.getCause();
            }
        });

        Map<Method, IdRef> refs = annotatedRefs(objCls);

        refs.forEach((m, ref) -> {
            String name = fieldPrefix + (ref.name().isEmpty() ? ObjectTools.getFieldName(m.getName()) : ref.name());
            Method getter = ObjectTools.getGetter(objCls, m);

            try {
                if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                    Optional.ofNullable((Collection<?>) getter.invoke(obj))
                        .ifPresent(vals -> {
                            LOGGER.debug("set ref {}=\"{}\" from {}", name, vals, getter);
                            vals.forEach(v -> Optional.ofNullable(buildReferenceId(v))
                                .ifPresent(refId -> doc.add(new StringField(name, refId.toString(), Store.YES))));
                        });
                } else {
                    Optional.ofNullable(getter.invoke(obj)).ifPresent(value -> {
                        LOGGER.debug("set ref {}=\"{}\" from {}", name, value, getter);
                        Optional.ofNullable(buildReferenceId(value))
                            .ifPresent(refId -> doc.add(new StringField(name, refId.toString(), Store.YES)));
                    });
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
                throw (RuntimeException) e.getCause();
            }
        });

        return doc;
    }

    private <T> Object buildReferenceId(T obj) {
        Class<?> objCls = obj.getClass();
        String fieldPrefix = objCls.getSimpleName() + ".";
        Map<Method, Field> mm = annotatedMethods(objCls);

        return mm.entrySet().stream().filter(e -> e.getKey().isAnnotationPresent(Id.class)).map(e -> {
            Method m = e.getKey();
            Field f = e.getValue();
            String name = fieldPrefix + (f.name().isEmpty() ? ObjectTools.getFieldName(m.getName()) : f.name());
            Method getter = ObjectTools.getGetter(objCls, m);

            try {
                if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                    return Optional.ofNullable((Collection<?>) getter.invoke(obj)).orElse(Collections.emptyList())
                        .stream()
                        .map(v -> {
                            try {
                                return Optional.ofNullable(marshal(f, v)).map(id -> name + ":" + id);
                            } catch (IllegalArgumentException ex) {
                                throw (RuntimeException) ex.getCause();
                            }
                        }).findFirst().orElse(null);
                } else {
                    return Optional.ofNullable(marshal(f, getter.invoke(obj))).map(id -> name + ":" + id);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException ex) {
                throw (RuntimeException) ex.getCause();
            }
        }).filter(Optional::isPresent).map(Optional::get).findFirst().orElse(null);
    }

    private <T> List<Object> buildReferences(T obj) {
        Class<?> objCls = obj.getClass();
        Map<Method, IdRef> refs = annotatedRefs(objCls);

        return refs.entrySet().stream().map(e -> {
            Method getter = ObjectTools.getGetter(objCls, e.getKey());
            try {
                if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                    return Optional.ofNullable((Collection<?>) getter.invoke(obj))
                        .map(vals -> vals.stream());
                } else {
                    return Optional.ofNullable(getter.invoke(obj)).map(Stream::of);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException ex) {
                throw (RuntimeException) ex.getCause();
            }
        }).filter(Optional::isPresent).map(Optional::get).flatMap(ds -> ds).distinct().collect(Collectors.toList());
    }

    private IndexableField buildIndexableField(Field a, String name, Object value) {
        if (value != null && a.index()) {
            Class<?> valCls = value.getClass();
            Store store = a.store() ? Store.YES : Store.NO;

            if (String.class.isAssignableFrom(valCls)) {
                return a.analyze() ? new TextField(name, (String) value, store)
                    : new StringField(name, (String) value, store);
            } else if (a.store() && valCls.isPrimitive()) {
                if (Integer.class.isAssignableFrom(valCls)) {
                    return new StoredField(name, (int) value);
                } else if (Long.class.isAssignableFrom(valCls)) {
                    return new StoredField(name, (long) value);
                } else if (Double.class.isAssignableFrom(valCls)) {
                    return new StoredField(name, (double) value);
                } else if (Float.class.isAssignableFrom(valCls)) {
                    return new StoredField(name, (float) value);
                }
            } else if (valCls.isPrimitive()) {
                if (Integer.class.isAssignableFrom(valCls)) {
                    return new IntPoint(name, (int) value);
                } else if (Long.class.isAssignableFrom(valCls)) {
                    return new LongPoint(name, (long) value);
                } else if (Double.class.isAssignableFrom(valCls)) {
                    return new DoublePoint(name, (double) value);
                } else if (Float.class.isAssignableFrom(valCls)) {
                    return new FloatPoint(name, (float) value);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T marshal(Field a, Object value) {
        try {
            return value != null ? (T) adapter(a).marshal(value) : null;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
            | IllegalArgumentException | InvocationTargetException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    public <T> List<T> get(Query query) {
        return get(query, DEFAULT_LIMIT);
    }

    public <T> List<T> get(Query query, int limit) {
        return getDocuments(query, limit).stream().map(this::<T> buildObject)
            .filter(o -> o != null)
            .collect(Collectors.toList());
    }

    public <T> boolean exists(T obj) {
        return !getDocuments(new TermQuery(buildIdTerm(obj)), 1).isEmpty();
    }

    private List<Document> getDocuments(Query query, int limit) {
        try {
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(query, limit);

            return Arrays.stream(docs.scoreDocs).map(h -> {
                try {
                    return searcher.doc(h.doc);
                } catch (IOException e) {
                    return null;
                }
            }).filter(o -> o != null).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T buildObject(Document doc) {
        try {
            Class<?> objCls = this.getClass().getClassLoader().loadClass(doc.get(OBJECT_CLASS_FIELD));
            String fieldPrefix = objCls.getSimpleName() + ".";
            T obj = (T) objCls.newInstance();
            Map<Method, Field> mm = annotatedMethods(objCls);

            mm.forEach((m, f) -> {
                String name = fieldPrefix + (f.name().isEmpty() ? ObjectTools.getFieldName(m.getName()) : f.name());
                Method getter = ObjectTools.getGetter(objCls, m);
                Method setter = ObjectTools.getSetter(objCls, m);

                try {
                    if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                        Collection<?> values = Arrays.stream(doc.getFields(name))
                            .map(v -> unmarshal(f, v)).filter(v -> v != null).collect(Collectors.toList());
                        LOGGER.debug("get {}=\"{}\" from {}", name, values, setter);
                        setter.invoke(obj, values);
                    } else {
                        Object value = unmarshal(f, doc.getField(name));
                        LOGGER.debug("get {}=\"{}\" from {}", name, value, setter);
                        setter.invoke(obj, value);
                    }
                } catch (SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                    throw (RuntimeException) e.getCause();
                }
            });

            Map<Method, IdRef> refs = annotatedRefs(objCls);

            refs.forEach((m, ref) -> {
                String name = fieldPrefix + (ref.name().isEmpty() ? ObjectTools.getFieldName(m.getName()) : ref.name());
                Method getter = ObjectTools.getGetter(objCls, m);
                Method setter = ObjectTools.getSetter(objCls, m);

                try {
                    if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                        Collection<?> values = Arrays.stream(doc.getFields(name)).map(v -> {
                            String[] id = v.stringValue().split(":", 2);
                            return get(new TermQuery(new Term(id[0], id[1])), 1).stream().findFirst();
                        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
                        LOGGER.debug("get ref {}=\"{}\" from {}", name, values, setter);
                        setter.invoke(obj, values);
                    } else {
                        Optional<Object> value = Optional.ofNullable(doc.getField(name)).map(v -> {
                            String[] id = v.stringValue().split(":", 2);
                            return get(new TermQuery(new Term(id[0], id[1])), 1).stream().findFirst();
                        }).orElseGet(Optional::empty);
                        LOGGER.debug("get ref {}=\"{}\" from {}", name, value.orElse(null), setter);
                        setter.invoke(obj, value.orElse(null));
                    }
                } catch (IllegalArgumentException | SecurityException | IllegalAccessException
                    | InvocationTargetException e) {
                    throw (RuntimeException) e.getCause();
                }
            });

            return obj;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    private <T> Term buildIdTerm(T obj) {
        Class<?> objCls = obj.getClass();
        String fieldPrefix = objCls.getSimpleName() + ".";
        Map<Method, Field> mm = annotatedMethods(objCls);

        return mm.entrySet().stream().filter(e -> e.getKey().isAnnotationPresent(Id.class)).map(e -> {
            Method m = e.getKey();
            Field f = e.getValue();
            String name = fieldPrefix + (f.name().isEmpty() ? ObjectTools.getFieldName(m.getName()) : f.name());
            Method getter = ObjectTools.getGetter(objCls, m);

            try {
                if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                    return Optional.ofNullable((Collection<?>) getter.invoke(obj))
                        .map(vals -> {
                            return vals.stream().map(
                                v -> Optional.ofNullable(marshal(f, v)).map(value -> new Term(name, value.toString())))
                                .filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);
                        }).orElse(null);
                } else {
                    return Optional.ofNullable(marshal(f, getter.invoke(obj)))
                        .map(value -> new Term(name, value.toString())).orElse(null);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException ex) {
                throw (RuntimeException) ex.getCause();
            }
        }).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    private <T> Object unmarshal(Field a, IndexableField f) {
        return f != null ? Stream.of(Optional.ofNullable(f.numericValue()),
            Optional.ofNullable(f.stringValue())).filter(Optional::isPresent).map(v -> {
                try {
                    return adapter(a).unmarshal(v.get());
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                    LOGGER.error(e.getMessage(), e);
                    return null;
                }
            }).filter(v -> v != null).findFirst().orElse(null) : null;
    }
}
