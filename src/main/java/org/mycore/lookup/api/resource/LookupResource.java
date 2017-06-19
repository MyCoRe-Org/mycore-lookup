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
package org.mycore.lookup.api.resource;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mycore.lookup.api.entity.Corporate;
import org.mycore.lookup.api.entity.Person;
import org.mycore.lookup.api.entity.Place;
import org.mycore.lookup.api.entity.wrapper.CorporateListWrapper;
import org.mycore.lookup.api.entity.wrapper.PersonListWrapper;
import org.mycore.lookup.api.entity.wrapper.PlaceListWrapper;
import org.mycore.lookup.api.service.LookupService;
import org.mycore.lookup.frontend.annotation.CacheMaxAge;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("lookup")
public class LookupResource {

    @GET
    @CacheMaxAge(time = 1, unit = TimeUnit.HOURS)
    @Path("corporates/{term}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public CorporateListWrapper corporates(@PathParam("term") String term) {
        return new CorporateListWrapper().setCorporates(LookupService.suggest(LookupService.Type.CORPORATE, term));
    }

    @GET
    @CacheMaxAge(time = 1, unit = TimeUnit.HOURS)
    @Path("corporate/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Corporate corporate(@PathParam("id") String id) {
        return LookupService.lookup(LookupService.Type.CORPORATE, id);
    }

    @GET
    @CacheMaxAge(time = 1, unit = TimeUnit.HOURS)
    @Path("persons/{term}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public PersonListWrapper persons(@PathParam("term") String term) {
        return new PersonListWrapper().setPersons(LookupService.suggest(LookupService.Type.PERSON, term));
    }

    @GET
    @CacheMaxAge(time = 1, unit = TimeUnit.HOURS)
    @Path("person/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Person person(@PathParam("id") String id) {
        return LookupService.lookup(LookupService.Type.PERSON, id);
    }

    @GET
    @CacheMaxAge(time = 1, unit = TimeUnit.HOURS)
    @Path("places/{term}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public PlaceListWrapper places(@PathParam("term") String term) {
        return new PlaceListWrapper().setPlaces(LookupService.suggest(LookupService.Type.PLACE, term));
    }

    @GET
    @CacheMaxAge(time = 1, unit = TimeUnit.HOURS)
    @Path("place/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Place place(@PathParam("id") String id) {
        return LookupService.lookup(LookupService.Type.PLACE, id);
    }
}
