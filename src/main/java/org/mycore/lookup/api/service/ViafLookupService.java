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

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAnyElement;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.mycore.lookup.api.entity.Corporate;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Person;
import org.mycore.lookup.api.entity.Place;
import org.mycore.lookup.api.entity.Scheme;
import org.mycore.lookup.api.rdf.RDFMapper;
import org.mycore.lookup.api.service.annotation.Service;
import org.mycore.lookup.frontend.provider.GenericExceptionMapper;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Service(type = LookupService.class)
@Priority(0)
public class ViafLookupService extends LookupService {

    private static final String REMOTE_URL = "http://www.viaf.org";

    private static final String REMOTE_PATH = "viaf";

    private static final Pattern DISPLAY_FORM_PATTERN = Pattern
        .compile("([^,]+),\\s?([^,]+(?=,)|[a-z-A-Z\\W\\-]+(?!,))(?:,\\s|\\s?)(?:(\\d+)\\??\\-(\\d+)?\\??)?");

    public ViafLookupService() {
        super();
        this.supportedSchemes.add(Scheme.get("viaf"));
    }

    private Function<String, WebTarget> buildTarget = (method) -> {
        ClientConfig config = new ClientConfig();
        config.register(MoxyJsonFeature.class);
        config.register(GenericExceptionMapper.class);

        return ClientBuilder.newClient(config).target(REMOTE_URL).path(REMOTE_PATH + "/" + method);
    };

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestCorporate(java.lang.String)
     */
    @Override
    public List<Corporate> suggestCorporate(String term) throws UnsupportedOperationException {
        WebTarget target = buildTarget.apply("AutoSuggest").queryParam("query", term);
        Response response = target.request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {
            ViafSuggest res = response.readEntity(ViafSuggest.class);

            return res.result.stream().filter(r -> ViafRecord.NameType.corporate.equals(r.nametype))
                .map(r -> mapRecordToCorporate(r)).collect(Collectors.toList());
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggest(java.lang.String)
     */
    @Override
    public List<Person> suggestPerson(String term) {
        WebTarget target = buildTarget.apply("AutoSuggest").queryParam("query", term);
        Response response = target.request(MediaType.APPLICATION_JSON).get();

        if (response.getStatus() == 200) {
            ViafSuggest res = response.readEntity(ViafSuggest.class);

            return res.result.stream().filter(r -> ViafRecord.NameType.personal.equals(r.nametype))
                .map(r -> mapRecordToPerson(r)).collect(Collectors.toList());
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestPlace(java.lang.String)
     */
    @Override
    public List<Place> suggestPlace(String term) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#corporate(java.lang.String)
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
     * @see org.mycore.lookup.api.service.LookupService#person(java.lang.String)
     */
    @Override
    public Person person(IdType idType) {
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
     * @see org.mycore.lookup.api.service.LookupService#place(java.lang.String)
     */
    @Override
    public Place place(IdType idType) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    private Person mapRecordToPerson(ViafRecord record) {
        if (record != null) {
            Person person = new Person();

            Matcher m = DISPLAY_FORM_PATTERN.matcher(record.displayForm);
            if (m.find()) {
                String sn = m.group(1).trim();
                String pn = m.group(2).trim();

                if (pn.isEmpty() && !sn.isEmpty()) {
                    String[] tmp = sn.split(" ", 2);
                    sn = tmp[0];
                    pn = tmp[1];
                }

                person.setFamilyName(sn);
                person.setGivenName(pn);

                Optional.ofNullable(m.group(3)).ifPresent(date -> person.setDateOfBirth(parseDate(date)));
                Optional.ofNullable(m.group(4)).ifPresent(date -> person.setDateOfDeath(parseDate(date)));
            }

            person.setMappedIds(record.ids.stream().filter(id -> id != null)
                .collect(Collectors.toList()));

            return person;
        }

        return null;
    }

    private Corporate mapRecordToCorporate(ViafRecord record) {
        if (record != null) {
            Corporate corporate = new Corporate();
            corporate.setName(record.displayForm);

            corporate.setMappedIds(record.ids.stream().filter(id -> id != null)
                .collect(Collectors.toList()));

            return corporate;
        }

        return null;
    }

    private TemporalAccessor parseDate(String str) {
        return DateTimeFormatter.ofPattern("yyyy[[-MM][-dd]]").parseBest(str, LocalDate::from,
            YearMonth::from, Year::from);
    }

    private static class ViafSuggest {

        public String query;

        public List<ViafRecord> result;

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            final int maxLen = 10;
            StringBuilder builder = new StringBuilder();
            builder.append("ViafSuggest [");
            if (query != null) {
                builder.append("query=");
                builder.append(query);
                builder.append(", ");
            }
            if (result != null) {
                builder.append("result=");
                builder.append(result.subList(0, Math.min(result.size(), maxLen)));
            }
            builder.append("]");
            return builder.toString();
        }

    }

    private static class ViafRecord {

        public String displayForm;

        public NameType nametype;

        @XmlAnyElement
        public List<IdType> ids;

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            final int maxLen = 10;
            StringBuilder builder = new StringBuilder();
            builder.append("ViafRecord [");
            if (displayForm != null) {
                builder.append("displayForm=");
                builder.append(displayForm);
                builder.append(", ");
            }
            if (nametype != null) {
                builder.append("nametype=");
                builder.append(nametype);
                builder.append(", ");
            }
            if (ids != null) {
                builder.append("ids=");
                builder.append(ids.subList(0, Math.min(ids.size(), maxLen)));
            }
            builder.append("]");
            return builder.toString();
        }

        private static enum NameType {
            personal, corporate;
        }
    }

}
