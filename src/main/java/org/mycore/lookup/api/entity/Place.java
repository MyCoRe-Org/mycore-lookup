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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.lookup.api.rdf.annotation.RDFMapping;
import org.mycore.lookup.backend.index.annotation.Field;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "place")
public class Place extends MappedIdentifiers<Place> {

    private String name;

    private String description;

    private String homepage;

    private List<String> alternateNames;

    /**
     * @return the name
     */
    @Field(analyze = true, store = true)
    @XmlElement
    @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#preferredNameForThePlaceOrGeographicName")
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    @Field(analyze = true, store = true)
    @XmlElement
    @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#definition")
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the homepage
     */
    @Field(store = true)
    @XmlElement
    @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#homepage")
    public String getHomepage() {
        return homepage;
    }

    /**
     * @param homepage the homepage to set
     */
    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    /**
     * @return the alternateNames
     */
    @Field(analyze = true, store = true)
    @XmlElement
    @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#variantNameForThePlaceOrGeographicName")
    public List<String> getAlternateNames() {
        return alternateNames;
    }

    /**
     * @param alternateNames the alternateNames to set
     */
    public void setAlternateNames(List<String> alternateNames) {
        this.alternateNames = alternateNames;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.entity.MappedIdentifiers#merge(java.lang.Object)
     */
    @Override
    public void merge(Place place) {
        this.setName(Optional.ofNullable(this.getName()).orElse(place.getName()));
        this.setDescription(Optional.ofNullable(this.getDescription()).orElse(place.getDescription()));
        this.setHomepage(Optional.ofNullable(this.getHomepage()).orElse(place.getHomepage()));

        boolean addName = Stream
            .concat(Arrays.stream(new String[] { this.getName() }),
                Arrays.stream(new ArrayList<>(
                    Optional.ofNullable(this.getAlternateNames()).orElse(Collections.emptyList())).stream()
                        .toArray(String[]::new)))
            .noneMatch(a -> a.equalsIgnoreCase(place.getName()));

        if (addName) {
            List<String> ans = new ArrayList<>(
                Optional.ofNullable(this.getAlternateNames()).orElse(Collections.emptyList()));
            ans.add(place.getName());
            ans.addAll(Optional.ofNullable(place.getAlternateNames()).orElse(Collections.emptyList()).stream()
                .filter(a -> !ans.contains(a)).collect(Collectors.toList()));
            this.setAlternateNames(ans);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final int maxLen = 10;
        StringBuilder builder = new StringBuilder();
        builder.append("Place [");
        if (name != null) {
            builder.append("name=");
            builder.append(name);
            builder.append(", ");
        }
        if (description != null) {
            builder.append("description=");
            builder.append(description);
            builder.append(", ");
        }
        if (alternateNames != null) {
            builder.append("alternateNames=");
            builder.append(alternateNames.subList(0, Math.min(alternateNames.size(), maxLen)));
            builder.append(", ");
        }
        if (mappedIds != null) {
            builder.append("mappedIds=");
            builder.append(mappedIds.subList(0, Math.min(mappedIds.size(), maxLen)));
        }
        builder.append("]");
        return builder.toString();
    }

}
