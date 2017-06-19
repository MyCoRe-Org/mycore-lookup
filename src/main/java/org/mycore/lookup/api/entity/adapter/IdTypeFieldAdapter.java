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
package org.mycore.lookup.api.entity.adapter;

import java.util.StringTokenizer;

import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Scheme;
import org.mycore.lookup.backend.index.adapter.FieldAdapter;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class IdTypeFieldAdapter extends FieldAdapter<String, IdType> {

    /* (non-Javadoc)
     * @see org.mycore.lookup.backend.index.adapter.FieldAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IdType unmarshal(String v) {
        StringTokenizer st = new StringTokenizer(v, ":");
        if (st.countTokens() == 2) {
            String type = st.nextToken();
            String id = st.nextToken();

            return new IdType(Scheme.get(type), id);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.backend.index.adapter.FieldAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(IdType v) {
        return v == null ? null : v.getScheme().getId() + ":" + v.getId();
    }

}
