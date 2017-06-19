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

import java.util.List;

import javax.annotation.Priority;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.TermQuery;
import org.mycore.lookup.api.entity.Corporate;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Person;
import org.mycore.lookup.api.entity.Place;
import org.mycore.lookup.api.entity.Scheme;
import org.mycore.lookup.api.service.annotation.Service;
import org.mycore.lookup.backend.index.IndexManager;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Service(type = LookupService.class)
@Priority(50)
public class IndexLookupService extends LookupService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final IndexManager idx;

    public IndexLookupService() {
        supportedSchemes = Scheme.getAll();
        idx = IndexManager.instance();
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestCorporate(java.lang.String)
     */
    @Override
    public List<Corporate> suggestCorporate(String term) throws UnsupportedOperationException {
        try {
            String query = "Corporate.alternateNames:" + term + " OR Corporate.description:" + term
                + " OR Corporate.name:" + term;
            return idx.get(new QueryParser("Corporate.name", idx.getAnalyzer()).parse(query));
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestPerson(java.lang.String)
     */
    @Override
    public List<Person> suggestPerson(String term) throws UnsupportedOperationException {
        try {
            String query = "Person.alternateNames:" + term + " OR Person.description:" + term + " OR Person.givenName:"
                + term + " OR Person.familyName:" + term;
            return idx.get(new QueryParser("Person.alternateNames", idx.getAnalyzer()).parse(query));
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestPlace(java.lang.String)
     */
    @Override
    public List<Place> suggestPlace(String term) throws UnsupportedOperationException {
        try {
            String query = "Place.name:" + term + " OR Place.alternateNames:" + term + " OR Place.description:" + term;
            return idx.get(new QueryParser("Place.name", idx.getAnalyzer()).parse(query));
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#corporate(java.lang.String)
     */
    @Override
    public Corporate corporate(IdType idType) throws UnsupportedOperationException {
        return (Corporate) idx.get(new TermQuery(new Term("Corporate.mappedIds", idType.toString())), 1)
            .stream().findFirst().orElse(null);
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#person(java.lang.String)
     */
    @Override
    public Person person(IdType idType) throws UnsupportedOperationException {
        return (Person) idx.get(new TermQuery(new Term("Person.mappedIds", idType.toString())), 1).stream().findFirst()
            .orElse(null);
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#place(java.lang.String)
     */
    @Override
    public Place place(IdType idType) throws UnsupportedOperationException {
        return (Place) idx.get(new TermQuery(new Term("Place.mappedIds", idType.toString())), 1)
            .stream().findFirst().orElse(null);
    }

}
