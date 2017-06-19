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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.mycore.lookup.api.entity.Corporate;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Scheme;
import org.mycore.lookup.api.rdf.RDFMapper;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class CorporateAdapter extends RDFMappingAdapter<String, Corporate> {

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Corporate unmarshal(String v) {
        if (v != null) {
            try {
                URI uri = new URI(v);
                Corporate c = RDFMapper.map(uri, Corporate.class);
                if (c == null) {
                    c = new Corporate();
                    c.setName(v);
                } else {
                    IdType idType = Scheme.extractIdTypeFromURI(uri);

                    if (c.getMappedIds() == null) {
                        c.setMappedIds(new ArrayList<>());
                    }
                    if (!c.getMappedIds().contains(idType)) {
                        c.getMappedIds().add(idType);
                    }
                }
                return c;
            } catch (URISyntaxException e) {
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(Corporate v) {
        return v != null ? v.getName() : null;
    }

}
