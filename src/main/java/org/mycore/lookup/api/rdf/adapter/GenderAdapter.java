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
package org.mycore.lookup.api.rdf.adapter;

import java.util.Arrays;

import org.mycore.lookup.api.entity.Person.Gender;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class GenderAdapter extends RDFMappingAdapter<String, Gender> {

    private static final String[] MALE_PATTERN = new String[] { "http://schema.org/Male",
        "http://d-nb.info/standards/vocab/gnd/gender#male",
        "http://www.wikidata.org/entity/Q6581097" };

    private static final String[] FEMALE_PATTERN = new String[] { "http://schema.org/Female",
        "http://d-nb.info/standards/vocab/gnd/gender#female",
        "http://www.wikidata.org/entity/Q6581072" };

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Gender unmarshal(String v) {
        return Arrays.stream(MALE_PATTERN).anyMatch(v::equalsIgnoreCase) ? Gender.male
            : Arrays.stream(FEMALE_PATTERN).anyMatch(v::equalsIgnoreCase) ? Gender.female : null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(Gender v) {
        return v == null ? null
            : Gender.male.equals(v) ? MALE_PATTERN[0] : Gender.female.equals(v) ? FEMALE_PATTERN[0] : null;
    }

}
