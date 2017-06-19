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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.mycore.lookup.api.entity.adapter.DateTemporalAccessorAdapter;
import org.mycore.lookup.api.entity.adapter.DateTemporalAccessorFieldAdapter;
import org.mycore.lookup.api.entity.adapter.GenderFieldAdapter;
import org.mycore.lookup.api.rdf.adapter.DateAdapter;
import org.mycore.lookup.api.rdf.adapter.GenderAdapter;
import org.mycore.lookup.api.rdf.adapter.Name2FamilyNameAdapter;
import org.mycore.lookup.api.rdf.adapter.Name2GivenNameAdapter;
import org.mycore.lookup.api.rdf.adapter.PlaceAdapter;
import org.mycore.lookup.api.rdf.adapter.PlacesAdapter;
import org.mycore.lookup.api.rdf.annotation.RDFMapping;
import org.mycore.lookup.api.rdf.annotation.RDFMapping.RDFMappings;
import org.mycore.lookup.backend.index.annotation.Field;
import org.mycore.lookup.backend.index.annotation.IdRef;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "person")
@XmlType(name = "person")
public class Person extends MappedIdentifiers<Person> {

    private Gender gender;

    private String description;

    private String familyName;

    private String givenName;

    private TemporalAccessor dateOfBirth;

    private TemporalAccessor dateOfDeath;

    private Place placeOfBirth;

    private Place placeOfDeath;

    private List<Place> placeOfActivity;

    private List<String> alternateNames;

    /**
     * @return the gender
     */
    @Field(store = true, adapter = GenderFieldAdapter.class)
    @XmlAttribute
    @RDFMappings({
        @RDFMapping(uri = "http://schema.org/gender", adapter = GenderAdapter.class),
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#gender", adapter = GenderAdapter.class)
    })
    public Gender getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * @return the description
     */
    @Field(analyze = true, store = true)
    @XmlElement
    @RDFMapping(uri = "http://schema.org/description")
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
     * @return the givenName
     */
    @Field(analyze = true, store = true)
    @XmlElement
    @RDFMappings({
        @RDFMapping(uri = "http://schema.org/name", adapter = Name2FamilyNameAdapter.class),
        @RDFMapping(uri = "http://schema.org/familyName"),
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#preferredNameForThePerson", adapter = Name2FamilyNameAdapter.class),
        @RDFMapping(nodeUri = "http://d-nb.info/standards/elementset/gnd#preferredNameEntityForThePerson", uri = "http://d-nb.info/standards/elementset/gnd#surname")
    })
    public String getFamilyName() {
        return familyName;
    }

    /**
     * @param familyName the givenName to set
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    /**
     * @return the givenName
     */
    @Field(analyze = true, store = true)
    @XmlElement
    @RDFMappings({
        @RDFMapping(uri = "http://schema.org/name", adapter = Name2GivenNameAdapter.class),
        @RDFMapping(uri = "http://schema.org/givenName"),
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#preferredNameForThePerson", adapter = Name2GivenNameAdapter.class),
        @RDFMapping(nodeUri = "http://d-nb.info/standards/elementset/gnd#preferredNameEntityForThePerson", uri = "http://d-nb.info/standards/elementset/gnd#forename")
    })
    public String getGivenName() {
        return givenName;
    }

    /**
     * @param givenName the givenName to set
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * @return the dateOfBirth
     */
    @Field(store = true, adapter = DateTemporalAccessorFieldAdapter.class)
    @XmlElement
    @XmlJavaTypeAdapter(DateTemporalAccessorAdapter.class)
    @RDFMappings({
        @RDFMapping(uri = "http://schema.org/birthDate", adapter = DateAdapter.class),
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#dateOfBirth", adapter = DateAdapter.class)
    })
    public TemporalAccessor getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth the dateOfBirth to set
     */
    public void setDateOfBirth(TemporalAccessor dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return the dateOfDeath
     */
    @Field(store = true, adapter = DateTemporalAccessorFieldAdapter.class)
    @XmlElement
    @XmlJavaTypeAdapter(DateTemporalAccessorAdapter.class)
    @RDFMappings({
        @RDFMapping(uri = "http://schema.org/deathDate", adapter = DateAdapter.class),
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#dateOfDeath", adapter = DateAdapter.class)
    })
    public TemporalAccessor getDateOfDeath() {
        return dateOfDeath;
    }

    /**
     * @param dateOfDeath the dateOfDeath to set
     */
    public void setDateOfDeath(TemporalAccessor dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    /**
     * @return the placeOfBirth
     */
    @IdRef
    @XmlElement()
    @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#placeOfBirth", adapter = PlaceAdapter.class)
    public Place getPlaceOfBirth() {
        return placeOfBirth;
    }

    /**
     * @param placeOfBirth the placeOfBirth to set
     */
    public void setPlaceOfBirth(Place placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    /**
     * @return the placeOfDeath
     */
    @IdRef
    @XmlElement()
    @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#placeOfDeath", adapter = PlaceAdapter.class)
    public Place getPlaceOfDeath() {
        return placeOfDeath;
    }

    /**
     * @param placeOfDeath the placeOfDeath to set
     */
    public void setPlaceOfDeath(Place placeOfDeath) {
        this.placeOfDeath = placeOfDeath;
    }

    /**
     * @return the placeOfActivity
     */
    @IdRef
    @XmlElement()
    @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#placeOfActivity", adapter = PlacesAdapter.class)
    public List<Place> getPlaceOfActivity() {
        return placeOfActivity;
    }

    /**
     * @param placeOfActivity the placeOfActivity to set
     */
    public void setPlaceOfActivity(List<Place> placeOfActivity) {
        this.placeOfActivity = placeOfActivity;
    }

    /**
     * @return the alternateNames
     */
    @Field(analyze = true, store = true)
    @XmlElement(name = "alternateName")
    @RDFMappings({
        @RDFMapping(uri = "http://schema.org/alternateName"),
        @RDFMapping(uri = "http://d-nb.info/standards/elementset/gnd#variantNameForThePerson")
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
    public void merge(Person person) {
        this.setGender(Optional.ofNullable(this.getGender()).orElse(person.getGender()));
        this.setDescription(Optional.ofNullable(this.getDescription()).orElse(person.getDescription()));
        this.setDateOfBirth(Optional.ofNullable(this.getDateOfBirth()).orElse(person.getDateOfBirth()));
        this.setDateOfDeath(Optional.ofNullable(this.getDateOfDeath()).orElse(person.getDateOfDeath()));
        this.setPlaceOfBirth(Optional.ofNullable(this.getPlaceOfBirth()).orElse(person.getPlaceOfBirth()));
        this.setPlaceOfDeath(Optional.ofNullable(this.getPlaceOfDeath()).orElse(person.getPlaceOfDeath()));
        this.setPlaceOfActivity(Optional.ofNullable(this.getPlaceOfActivity()).orElse(person.getPlaceOfActivity()));

        this.getMappedIds().addAll(person.getMappedIds().stream().filter(id -> !this.getMappedIds().contains(id))
            .collect(Collectors.toList()));

        boolean addName = Stream
            .concat(Arrays.stream(new String[] { this.getGivenName() + " " + this.getFamilyName() }),
                Arrays.stream(new ArrayList<>(
                    Optional.ofNullable(this.getAlternateNames()).orElse(Collections.emptyList())).stream()
                        .toArray(String[]::new)))
            .noneMatch(a -> a.equalsIgnoreCase(person.getGivenName() + " " + person.getFamilyName()));

        if (addName) {
            List<String> ans = new ArrayList<>(
                Optional.ofNullable(this.getAlternateNames()).orElse(Collections.emptyList()));
            ans.add(person.getGivenName() + " " + person.getFamilyName());
            ans.addAll(Optional.ofNullable(person.getAlternateNames()).orElse(Collections.emptyList()).stream()
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
        builder.append("Person [");
        if (gender != null) {
            builder.append("gender=");
            builder.append(gender);
            builder.append(", ");
        }
        if (description != null) {
            builder.append("description=");
            builder.append(description);
            builder.append(", ");
        }
        if (familyName != null) {
            builder.append("familyName=");
            builder.append(familyName);
            builder.append(", ");
        }
        if (givenName != null) {
            builder.append("givenName=");
            builder.append(givenName);
            builder.append(", ");
        }
        if (dateOfBirth != null) {
            builder.append("dateOfBirth=");
            builder.append(dateOfBirth);
            builder.append(", ");
        }
        if (dateOfDeath != null) {
            builder.append("dateOfDeath=");
            builder.append(dateOfDeath);
            builder.append(", ");
        }
        if (placeOfBirth != null) {
            builder.append("placeOfBirth=");
            builder.append(placeOfBirth);
            builder.append(", ");
        }
        if (placeOfDeath != null) {
            builder.append("placeOfDeath=");
            builder.append(placeOfDeath);
            builder.append(", ");
        }
        if (mappedIds != null) {
            builder.append("mappedIds=");
            builder.append(mappedIds.subList(0, Math.min(mappedIds.size(), maxLen)));
        }
        builder.append("]");
        return builder.toString();
    }

    @XmlEnum
    @XmlType(name = "gender")
    public static enum Gender {
        male, female;
    }

    @XmlRootElement
    public static class AlternateName {
        private String familyName;

        private String givenName;

        public AlternateName() {
            super();
        }

        /**
         * @param familyName
         * @param givenName
         */
        public AlternateName(String familyName, String givenName) {
            super();
            this.familyName = familyName;
            this.givenName = givenName;
        }

        /**
         * @return the familyName
         */
        @XmlElement
        public String getFamilyName() {
            return familyName;
        }

        /**
         * @param familyName the familyName to set
         */
        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        /**
         * @return the givenName
         */
        @XmlElement
        public String getGivenName() {
            return givenName;
        }

        /**
         * @param givenName the givenName to set
         */
        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AlternateName [");
            if (familyName != null) {
                builder.append("familyName=");
                builder.append(familyName);
                builder.append(", ");
            }
            if (givenName != null) {
                builder.append("givenName=");
                builder.append(givenName);
            }
            builder.append("]");
            return builder.toString();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((givenName == null) ? 0 : givenName.hashCode());
            result = prime * result + ((familyName == null) ? 0 : familyName.hashCode());
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
            if (!(obj instanceof AlternateName)) {
                return false;
            }
            AlternateName other = (AlternateName) obj;
            if (givenName == null) {
                if (other.givenName != null) {
                    return false;
                }
            } else if (!givenName.equals(other.givenName)) {
                return false;
            }
            if (familyName == null) {
                if (other.familyName != null) {
                    return false;
                }
            } else if (!familyName.equals(other.familyName)) {
                return false;
            }
            return true;
        }

    }
}
