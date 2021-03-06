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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mycore.lookup.api.entity.Corporate;
import org.mycore.lookup.api.entity.Person;
import org.mycore.lookup.api.entity.Place;
import org.mycore.lookup.api.service.LookupService.Type;
import org.mycore.lookup.common.config.Configuration;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestLookupService {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        Configuration config = Configuration.instance();
        config.set("Index.Path", tmpFolder.newFolder("index").getAbsolutePath());
    }

    @Test
    public void testSuggestPerson() {
        List<Person> persons = LookupService.suggest(Type.PERSON, "scheffler, thomas");

        assertFalse(persons.isEmpty());
    }

    @Test
    public void testLookupPerson() {
        Person person = LookupService.lookup(Type.PERSON, "gnd:135799082");

        assertNotNull(person);
    }

    @Test
    public void testSuggestCorporate() {
        List<Corporate> corporates = LookupService.suggest(Type.CORPORATE, "FSU Jena");

        assertFalse(corporates.isEmpty());
    }

    @Test
    public void testLookupCorporate() {
        Corporate corporate = LookupService.lookup(Type.CORPORATE, "gnd:36164-1");

        assertNotNull(corporate);
    }

    @Test
    public void testSuggestPlace() {
        List<Place> places = LookupService.suggest(Type.PLACE, "Jena");

        assertFalse(places.isEmpty());
    }

    @Test
    public void testLookupPlace() {
        Place place = LookupService.lookup(Type.PLACE, "gnd:4028557-1");

        assertNotNull(place);
    }
}
