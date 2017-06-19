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

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.mycore.lookup.api.entity.adapter.DateTemporalAccessorAdapter;
import org.mycore.lookup.api.entity.adapter.DateTemporalAccessorFieldAdapter;
import org.mycore.lookup.api.rdf.adapter.CorporateAdapter;
import org.mycore.lookup.api.rdf.adapter.DateAdapter;
import org.mycore.lookup.api.rdf.adapter.PlaceAdapter;
import org.mycore.lookup.api.rdf.annotation.RDFMapping;
import org.mycore.lookup.api.rdf.annotation.RDFMapping.RDFMappings;
import org.mycore.lookup.backend.index.annotation.Field;
import org.mycore.lookup.backend.index.annotation.IdRef;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "corporate")
@XmlType(name = "corporate")
public class Corporate extends MappedIdentifiers<Corporate> {

    private String name;

    private TemporalAccessor dateOfEstablishment;

    private TemporalAccessor dateOfTermination;

    private String homepage;

    private Corporate parent;

    private Place place;

    private List<String> alternateNames;

    /**
     * @return the name
     */
    @Field(analyze = true, store = true)
    @XmlElement
    @RDFMappings({
        @RDFMapping(uri = "http://schema.org/name"),
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#preferredNameForTheCorporateBody")
    })
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
     * @return the dateOfEstablishment
     */
    @Field(store = true, adapter = DateTemporalAccessorFieldAdapter.class)
    @XmlElement
    @XmlJavaTypeAdapter(DateTemporalAccessorAdapter.class)
    @RDFMappings({
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#dateOfEstablishment", adapter = DateAdapter.class)
    })
    public TemporalAccessor getDateOfEstablishment() {
        return dateOfEstablishment;
    }

    /**
     * @param dateOfEstablishment the dateOfEstablishment to set
     */
    public void setDateOfEstablishment(TemporalAccessor dateOfEstablishment) {
        this.dateOfEstablishment = dateOfEstablishment;
    }

    /**
     * @return the dateOfTermination
     */
    @Field(store = true, adapter = DateTemporalAccessorFieldAdapter.class)
    @XmlElement
    @XmlJavaTypeAdapter(DateTemporalAccessorAdapter.class)
    @RDFMappings({
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#dateOfTermination", adapter = DateAdapter.class)
    })
    public TemporalAccessor getDateOfTermination() {
        return dateOfTermination;
    }

    /**
     * @param dateOfTermination the dateOfTermination to set
     */
    public void setDateOfTermination(TemporalAccessor dateOfTermination) {
        this.dateOfTermination = dateOfTermination;
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
     * @return the parent
     */
    @IdRef
    @XmlElement
    @RDFMappings({
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#hierarchicalSuperiorOfTheCorporateBody", adapter = CorporateAdapter.class)
    })
    public Corporate getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Corporate parent) {
        this.parent = parent;
    }

    /**
     * @return the place
     */
    @IdRef
    @XmlElement
    @RDFMappings({
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#placeOfBusiness", adapter = PlaceAdapter.class)
    })
    public Place getPlace() {
        return place;
    }

    /**
     * @param place the place to set
     */
    public void setPlace(Place place) {
        this.place = place;
    }

    /**
     * @return the alternateNames
     */
    @Field(analyze = true, store = true)
    @XmlElement(name = "alternateName")
    @RDFMappings({
        @RDFMapping(uri = "http://schema.org/alternateName"),
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#variantNameForTheCorporateBody")
    })
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
    public void merge(Corporate corporate) {
        this.setName(Optional.ofNullable(this.getName()).orElse(corporate.getName()));
        this.setDateOfEstablishment(
            Optional.ofNullable(this.getDateOfEstablishment()).orElse(corporate.getDateOfEstablishment()));
        this.setDateOfTermination(
            Optional.ofNullable(this.getDateOfTermination()).orElse(corporate.getDateOfTermination()));
        this.setHomepage(Optional.ofNullable(this.getHomepage()).orElse(corporate.getHomepage()));
        this.setParent(Optional.ofNullable(this.getParent()).orElse(corporate.getParent()));
        this.setPlace(Optional.ofNullable(this.getPlace()).orElse(corporate.getPlace()));

        boolean addName = Stream
            .concat(Arrays.stream(new String[] { this.getName() }),
                Arrays.stream(new ArrayList<>(
                    Optional.ofNullable(this.getAlternateNames()).orElse(Collections.emptyList())).stream()
                        .toArray(String[]::new)))
            .noneMatch(a -> a.equalsIgnoreCase(corporate.getName()));

        if (addName) {
            List<String> ans = new ArrayList<>(
                Optional.ofNullable(this.getAlternateNames()).orElse(Collections.emptyList()));
            ans.add(corporate.getName());
            ans.addAll(Optional.ofNullable(corporate.getAlternateNames()).orElse(Collections.emptyList()).stream()
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
        builder.append("Corporate [");
        if (name != null) {
            builder.append("name=");
            builder.append(name);
            builder.append(", ");
        }
        if (place != null) {
            builder.append("place=");
            builder.append(place);
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
