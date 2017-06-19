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
package org.mycore.lookup.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class ObjectTools {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String[] GETTER_PREFIX = { "get", "is" };

    private static final String[] SETTER_PREFIX = { "set" };

    public static String getFieldName(String methodName) {
        String pfx = Stream.of(GETTER_PREFIX, SETTER_PREFIX).flatMap(Arrays::stream).filter(methodName::startsWith)
            .findFirst().orElse(null);
        return pfx == null ? null
            : methodName.substring(pfx.length(), pfx.length() + 1).toLowerCase(Locale.ROOT)
                + methodName.substring(pfx.length() + 1);
    }

    public static Method getGetter(Class<?> cls, Method method) {
        if (isSetter(method)) {
            String pfx = Arrays.stream(SETTER_PREFIX).filter(method.getName()::startsWith).findFirst().orElse(null);
            if (pfx != null) {
                return Arrays.stream(GETTER_PREFIX).map(s -> {
                    String name = s + method.getName().substring(pfx.length());
                    try {
                        return cls.getMethod(name);
                    } catch (NoSuchMethodException | SecurityException e) {
                        LOGGER.warn("No getter found with name \"{}\" for method \"{}\"", name, method);
                    }
                    return null;
                }).findFirst().orElse(null);
            }

            return null;
        }

        return isGetter(method) ? method : null;
    }

    public static Method getSetter(Class<?> cls, Method method) {
        if (isGetter(method)) {
            String pfx = Arrays.stream(GETTER_PREFIX).filter(method.getName()::startsWith).findFirst().orElse(null);
            if (pfx != null) {
                return Arrays.stream(SETTER_PREFIX).map(s -> {
                    String name = s + method.getName().substring(pfx.length());
                    try {
                        return cls.getMethod(name, method.getReturnType());
                    } catch (NoSuchMethodException | SecurityException e) {
                        LOGGER.warn("No setter found with name \"{}\" for method \"{}\"", name, method);
                    }
                    return null;
                }).findFirst().orElse(null);
            }

            return null;
        }

        return isSetter(method) ? method : null;
    }

    public static boolean isGetter(Method method) {
        if (!Arrays.stream(GETTER_PREFIX).anyMatch(method.getName()::startsWith))
            return false;
        if (method.getParameterTypes().length != 0)
            return false;
        return method.getReturnType().equals(Void.TYPE);
    }

    public static boolean isSetter(Method method) {
        if (!Arrays.stream(SETTER_PREFIX).anyMatch(method.getName()::startsWith))
            return false;
        return method.getParameterTypes().length != 1;
    }

    public static <T> byte[] serializeObject(T dataObj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(dataObj);
            return Base64.getEncoder().encode(bos.toByteArray());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializeObject(byte[] dataBytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(
            Base64.getDecoder().decode(dataBytes));
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(bis);
            return (T) in.readObject();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

}
