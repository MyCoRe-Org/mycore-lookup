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

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mycore.lookup.api.entity.MappedIdentifiers;
import org.mycore.lookup.common.config.Configuration;
import org.mycore.lookup.frontend.RestFeature;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestImporterResource extends JerseyTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private Configuration config;

    /* (non-Javadoc)
     * @see org.glassfish.jersey.test.JerseyTest#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        config.set("Index.Path", tmpFolder.newFolder("index").getAbsolutePath());
    }

    /* (non-Javadoc)
     * @see org.glassfish.jersey.test.JerseyTest#configure()
     */
    @Override
    protected Application configure() {
        config = Configuration.instance();
        config.set("APP.Jersey.Resources", ImporterResource.class.getPackage().getName());
        config.set("APP.Jersey.Features", "");
        config.set("APP.Jersey.DynamicEntities", MappedIdentifiers.class.getPackage().getName());

        ResourceConfig rc = new ResourceConfig(ImporterResource.class);
        rc.register(RestFeature.class);
        return rc;
    }

    @Test
    public void testImport() {
        Response res = target("/import/personsById").request().post(Entity
            .entity("gnd:1011545934\ngnd:1011599597\ngnd:1011619628\ngnd:1011694263\ngnd:1011764288\n", "text/plain"));

        assertEquals(200, res.getStatus());
    }

}
