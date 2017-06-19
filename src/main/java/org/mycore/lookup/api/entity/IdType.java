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
package org.mycore.lookup.api.entity;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.mycore.lookup.api.entity.adapter.IdTypeAdapter;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlJavaTypeAdapter(IdTypeAdapter.class)
public class IdType {

    private static final Pattern SCHEME_ID_PATTERN = Pattern.compile("^([^:]+):(.*)$");

    private Scheme scheme;

    private String id;

    public static IdType parse(String id) {
        Matcher m = SCHEME_ID_PATTERN.matcher(id);
        if (m.find()) {
            return new IdType(Scheme.get(m.group(1)), m.group(2));
        }
        return null;
    }

    /**
     * 
     */
    public IdType() {
        super();
    }

    /**
     * @param scheme
     * @param id
     */
    public IdType(Scheme scheme, String id) {
        this.scheme = scheme;
        this.id = id;
    }

    /**
     * @return the scheme
     */
    @XmlTransient
    public Scheme getScheme() {
        return scheme;
    }

    /**
     * @param scheme the scheme to set
     */
    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

    /**
     * @return the id
     */
    @XmlValue
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public URI authorityURI() {
        return scheme.buildAuthorityURI(id);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return scheme != null && id != null ? scheme.getId() + ":" + id : null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IdType)) {
            return false;
        }
        IdType other = (IdType) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (scheme == null) {
            if (other.scheme != null) {
                return false;
            }
        } else if (!scheme.equals(other.scheme)) {
            return false;
        }
        return true;
    }

}
