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
package org.mycore.lookup.api.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Year;

import org.junit.Test;
import org.mycore.lookup.api.entity.Corporate;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Person;
import org.mycore.lookup.api.entity.Person.Gender;
import org.mycore.lookup.api.entity.Scheme;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestRDFMapper {

    @Test
    public void testGNDPerson() {
        IdType idType = new IdType(Scheme.get("gnd"), "135799082");
        assertNotNull(idType.authorityURI());

        Person person = RDFMapper.map(idType.authorityURI(), Person.class);
        assertNotNull(person);

        assertEquals(Gender.female, person.getGender());
        assertEquals(Year.parse("1979"), person.getDateOfBirth());
        assertEquals("Neumann", person.getFamilyName());
        assertEquals("Dohna", person.getPlaceOfBirth().getName());
    }

    @Test
    public void testGNDCorporate() {
        IdType idType = new IdType(Scheme.get("gnd"), "36164-1");
        assertNotNull(idType.authorityURI());

        Corporate corporate = RDFMapper.map(idType.authorityURI(), Corporate.class);
        assertNotNull(corporate);

        assertEquals(Year.parse("1934"), corporate.getDateOfEstablishment());
        assertEquals("Friedrich-Schiller-Universität Jena", corporate.getName());
    }

    @Test
    public void testViafPerson() {
        IdType idType = new IdType(Scheme.get("viaf"), "80256051");
        assertNotNull(idType.authorityURI());

        Person person = RDFMapper.map(idType.authorityURI(), Person.class);
        assertNotNull(person);

        assertEquals(Gender.female, person.getGender());
        assertEquals(Year.parse("1979"), person.getDateOfBirth());
    }

    @Test
    public void testViafCorporate() {
        IdType idType = new IdType(Scheme.get("viaf"), "153161828");
        assertNotNull(idType.authorityURI());

        Corporate corporate = RDFMapper.map(idType.authorityURI(), Corporate.class);
        assertNotNull(corporate);

        assertTrue(corporate.getName().contains("Jena"));
    }

}
