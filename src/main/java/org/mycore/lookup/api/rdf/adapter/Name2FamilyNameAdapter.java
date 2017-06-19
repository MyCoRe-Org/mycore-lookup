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

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class Name2FamilyNameAdapter extends RDFMappingAdapter<String, String> {

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public String unmarshal(String v) {
        try {
            if (v.contains(",")) {
                return v.substring(0, v.lastIndexOf(",")).trim();
            }

            return v.substring(v.lastIndexOf(" ") + 1).trim();
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(String v) {
        // Nearly impossible to marshal a single familyName back to name.
        return null;
    }

}
