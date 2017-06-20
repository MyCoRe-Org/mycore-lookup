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

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.mycore.lookup.api.service.LookupService;
import org.mycore.lookup.api.service.LookupService.Type;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("import")
public class ImporterResource {

    @POST
    @Path("personsById")
    public Response importIds(String text) throws IOException {
        StringTokenizer st = new StringTokenizer(text);
        Stream.generate(() -> st.hasMoreTokens() ? st.nextToken() : null).filter(s -> s != null)
            .forEach(id -> {
                LogManager.getLogger().info("Import {}", id);
                LookupService.lookup(Type.PERSON, id);
            });
        return Response.ok().build();
    }
}
