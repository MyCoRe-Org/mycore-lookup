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
package org.mycore.lookup.api.rdf.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter;

@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public @interface RDFMapping {

    /**
     * The optional containing node URI.
     */
    String nodeUri() default "";

    /**
     * The schema URI.
     */
    String uri();

    /**
     * Specify if mapping is required.
     */
    boolean required() default false;

    @SuppressWarnings("rawtypes")
    Class<? extends RDFMappingAdapter> adapter() default DEFAULT_ADAPTER.class;

    @Documented
    @Retention(RUNTIME)
    @Target({ FIELD, METHOD })
    public @interface RDFMappings {
        RDFMapping[] value();
    }

    static final class DEFAULT_ADAPTER extends RDFMappingAdapter<Object, Object> {

        /* (non-Javadoc)
         * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#unmarshal(java.lang.Object)
         */
        @Override
        public Object unmarshal(Object v) {
            return v;
        }

        /* (non-Javadoc)
         * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#marshal(java.lang.Object)
         */
        @Override
        public Object marshal(Object v) {
            return v;
        }

    }
}
