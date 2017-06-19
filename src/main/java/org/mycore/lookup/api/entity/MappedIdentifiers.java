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

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.lookup.api.entity.adapter.IdTypeFieldAdapter;
import org.mycore.lookup.api.rdf.adapter.SameAsAdapter;
import org.mycore.lookup.api.rdf.annotation.RDFMapping;
import org.mycore.lookup.api.rdf.annotation.RDFMapping.RDFMappings;
import org.mycore.lookup.backend.index.annotation.Field;
import org.mycore.lookup.backend.index.annotation.Id;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement
public abstract class MappedIdentifiers<T> {

    protected List<IdType> mappedIds;

    /**
     * @return the mappedIds
     */
    @Id
    @Field(store = true, adapter = IdTypeFieldAdapter.class)
    @XmlElementWrapper(name = "mappedIds")
    @XmlAnyElement
    // @XmlJavaTypeAdapter(MappedIdAdapter.class)
    @RDFMappings({
        @RDFMapping(uri = "http://schema.org/sameAs", adapter = SameAsAdapter.class),
        @RDFMapping(uri = "http://www.w3.org/2002/07/owl#sameAs", adapter = SameAsAdapter.class)
    })
    public List<IdType> getMappedIds() {
        return mappedIds;
    }

    /**
     * @param mappedIds the mappedIds to set
     */
    public void setMappedIds(List<IdType> mappedIds) {
        this.mappedIds = mappedIds;
    }

    /**
     * Merge object together.
     * 
     * @param obj the object to from
     */
    public abstract void merge(T obj);
}
