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
package org.mycore.lookup.backend.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Person;
import org.mycore.lookup.api.entity.Person.Gender;
import org.mycore.lookup.api.entity.Place;
import org.mycore.lookup.api.entity.Scheme;
import org.mycore.lookup.common.config.Configuration;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestIndexManager {

    private static final int SLEEP_TIME = 250;

    private IndexManager IDX_MGR;

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        Configuration config = Configuration.instance();
        config.set("Index.Path", tmpFolder.newFolder("index").getAbsolutePath());

        IDX_MGR = IndexManager.instance();
    }

    @After
    public void tearDown() {
        IDX_MGR.close();
    }

    @Test
    public void testAdd() throws IOException, ParseException {
        Person p = buildPerson();
        p.setPlaceOfBirth(buildPlace());
        p.setPlaceOfActivity(Arrays.asList(buildPlace()));
        IDX_MGR.set(p);

        waitForWriteActionDone();

        Query q = new TermQuery(new Term("Person.mappedIds", "DNB:1234567890"));

        List<Person> hits = IDX_MGR.get(q);
        hits.forEach(LogManager.getLogger()::info);

        assertEquals(1, hits.size());
        assertTrue(IDX_MGR.exists(p));
    }

    @Test
    public void testUpdate() throws IOException, ParseException {
        Person p = buildPerson();
        p.setPlaceOfBirth(buildPlace());
        p.setPlaceOfActivity(Arrays.asList(buildPlace()));
        IDX_MGR.set(p);

        waitForWriteActionDone();

        p.setDescription("Updated Person Object.");
        IDX_MGR.set(p);

        waitForWriteActionDone();

        Query q = new TermQuery(new Term("Person.mappedIds", "DNB:1234567890"));

        Person p2 = (Person) Optional.ofNullable(IDX_MGR.get(q, 1))
            .map(l -> l.stream().peek(LogManager.getLogger()::info).findFirst().orElse(null))
            .orElse(null);

        assertNotNull(p2);
        assertEquals("Updated Person Object.", p2.getDescription());
    }

    private void waitForWriteActionDone() {
        Instant start = Instant.now();

        boolean working = true;
        while (working) {
            if (start.plus(IDX_MGR.writeExecutor.getTaskCount() * SLEEP_TIME * 2, ChronoUnit.MILLIS)
                .isAfter(Instant.now())) {
                synchronized (IDX_MGR.writeExecutor) {
                    try {
                        IDX_MGR.writeExecutor.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                working = IDX_MGR.writeExecutor.getTaskCount() - IDX_MGR.writeExecutor.getCompletedTaskCount() > 0
                    && IDX_MGR.writeExecutor.getActiveCount() != 0;
            } else {
                working = false;
            }
        }
    }

    private Person buildPerson() {
        Person p = new Person();
        p.setGender(Gender.female);
        p.setDescription("Only a test person object with wrong id's.");
        p.setGivenName("Susi");
        p.setFamilyName("Tester");
        p.setDateOfBirth(YearMonth.parse("1977-11"));
        p.setAlternateNames(Arrays.asList("Tester, Susanne", "Tester, S."));

        p.setMappedIds(
            Arrays.asList(new IdType(Scheme.get("dnb"), "1234567890"), new IdType(Scheme.get("viaf"), "1234567890")));

        return p;
    }

    private Place buildPlace() {
        Place p = new Place();
        p.setName("Testheim");
        p.setHomepage("http://www.mycore.de");

        p.setMappedIds(
            Arrays.asList(new IdType(Scheme.get("dnb"), "123456789X"), new IdType(Scheme.get("viaf"), "123456789X")));

        return p;
    }
}
