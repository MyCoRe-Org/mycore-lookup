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
package org.mycore.lookup.api.service;

import java.util.ArrayList;
import java.util.List;

import org.mycore.lookup.api.entity.Corporate;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Person;
import org.mycore.lookup.api.entity.Place;
import org.mycore.lookup.api.entity.Scheme;
import org.mycore.lookup.api.rdf.RDFMapper;
import org.mycore.lookup.api.service.annotation.Service;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Service(type = LookupService.class)
public class LoCLookupService extends LookupService {

    public LoCLookupService() {
        super();
        this.supportedSchemes.add(Scheme.get("lc"));
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestCorporate(java.lang.String)
     */
    @Override
    public List<Corporate> suggestCorporate(String term) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestPerson(java.lang.String)
     */
    @Override
    public List<Person> suggestPerson(String term) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestPlace(java.lang.String)
     */
    @Override
    public List<Place> suggestPlace(String term) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#corporate(org.mycore.lookup.api.entity.IdType)
     */
    @Override
    public Corporate corporate(IdType idType) throws UnsupportedOperationException {
        Corporate c = RDFMapper.map(idType.getScheme().buildAuthorityURI(idType.getId()), Corporate.class);

        if (c != null) {
            if (c.getMappedIds() == null) {
                c.setMappedIds(new ArrayList<>());
            }
            if (!c.getMappedIds().contains(idType)) {
                c.getMappedIds().add(idType);
            }
        }

        return c;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#person(org.mycore.lookup.api.entity.IdType)
     */
    @Override
    public Person person(IdType idType) throws UnsupportedOperationException {
        Person p = RDFMapper.map(idType.getScheme().buildAuthorityURI(idType.getId()), Person.class);

        if (p != null) {
            if (p.getMappedIds() == null) {
                p.setMappedIds(new ArrayList<>());
            }
            if (!p.getMappedIds().contains(idType)) {
                p.getMappedIds().add(idType);
            }
        }

        return p;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#place(org.mycore.lookup.api.entity.IdType)
     */
    @Override
    public Place place(IdType idType) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
