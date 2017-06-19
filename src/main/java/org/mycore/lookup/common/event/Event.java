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

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class Event<T> {

    private String type;

    private T object;

    private Class<?> source;

    public Event(final String type) {
        this(type, null, null);
    }

    public Event(final String type, Class<?> source) {
        this(type, null, source);
    }

    public Event(final String type, T object) {
        this(type, object, null);
    }

    public Event(final String type, T object, Class<?> source) {
        this.type = type;
        this.object = object;
        this.source = source;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the object
     */
    public T getObject() {
        return object;
    }

    /**
     * @param object
     *            the object to set
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * @return the source
     */
    public Class<?> getSource() {
        return source;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Event [type=" + type + ", object=" + (object != null ? object.getClass() : null) + ", source=" + source
            + "]";
    }

}
