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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.json.JsonArray;
import javax.json.JsonString;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
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
@Priority(100)
public class DNBLookupService extends LookupService {

    private static final String REMOTE_URL = "http://ws.gbv.de";

    private static final Pattern PATTERN_BASE_URI = Pattern.compile("http:\\/\\/d-nb\\.info\\/gnd\\/([\\w]+)");

    private static final Pattern PATTERN_DISPLAY_FORM = Pattern
        .compile("([^,]+),\\s?([^,]+(?=,)|[a-z-A-Z\\W\\-]+(?!,))(?:,\\s|\\s?)(?:(\\d+)\\??\\-(\\d+)?\\??)?");

    private static final String TYPE_CORPORATE = "CorporateBody";

    private static final String TYPE_PERSON = "DifferentiatedPerson";

    private static final String TYPE_PLACE = "PlaceOrGeographicName";

    private Function<String, WebTarget> buildTarget = (method) -> {
        ClientConfig config = new ClientConfig();
        config.register(JsonProcessingFeature.class);
        config.register(GenericExceptionMapper.class);
    
        return ClientBuilder.newClient(config).target(REMOTE_URL).path("/" + method);
    };

    public DNBLookupService() {
        super();
        this.supportedSchemes.add(Scheme.get("gnd"));
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestCorporate(java.lang.String)
     */
    @Override
    public List<Corporate> suggestCorporate(String term) throws UnsupportedOperationException {
        WebTarget target = buildTarget.apply("suggest/gnd/").queryParam("searchterm", term).queryParam("type",
            TYPE_CORPORATE);
        JsonArray data = target.request(MediaType.APPLICATION_JSON).get(JsonArray.class);

        if (data.size() == 4) {
            JsonArray names = data.getJsonArray(1);
            JsonArray types = data.getJsonArray(2);
            JsonArray uris = data.getJsonArray(3);

            return IntStream.range(0, names.size())
                .filter(i -> ((JsonString) types.get(i)).getChars().equals(TYPE_CORPORATE)).mapToObj(i -> {
                    Corporate corporate = new Corporate();
                    corporate.setName(((JsonString) names.get(i)).getString().trim());

                    Matcher m = PATTERN_BASE_URI.matcher(((JsonString) uris.get(i)).getChars());
                    if (m.find()) {
                        corporate.setMappedIds(
                            Stream.of(new IdType(Scheme.get("gnd"), m.group(1).trim())).collect(Collectors.toList()));
                    }

                    return corporate;
                }).collect(Collectors.toList());
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestPerson(java.lang.String)
     */
    @Override
    public List<Person> suggestPerson(String term) throws UnsupportedOperationException {
        WebTarget target = buildTarget.apply("suggest/gnd/").queryParam("searchterm", term).queryParam("type",
            TYPE_PERSON);
        JsonArray data = target.request(MediaType.APPLICATION_JSON).get(JsonArray.class);

        if (data.size() == 4) {
            JsonArray names = data.getJsonArray(1);
            JsonArray types = data.getJsonArray(2);
            JsonArray uris = data.getJsonArray(3);

            return IntStream.range(0, names.size())
                .filter(i -> ((JsonString) types.get(i)).getChars().equals(TYPE_PERSON)).mapToObj(i -> {
                    Person person = new Person();
                    Matcher m = PATTERN_DISPLAY_FORM.matcher(((JsonString) names.get(i)).getChars());
                    if (m.find()) {
                        person.setFamilyName(m.group(1).trim());
                        person.setGivenName(m.group(2).trim());
                    }

                    m = PATTERN_BASE_URI.matcher(((JsonString) uris.get(i)).getChars());
                    if (m.find()) {
                        person.setMappedIds(
                            Stream.of(new IdType(Scheme.get("gnd"), m.group(1).trim())).collect(Collectors.toList()));
                    }

                    return person;
                }).collect(Collectors.toList());
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#suggestPerson(java.lang.String)
     */
    @Override
    public List<Place> suggestPlace(String term) throws UnsupportedOperationException {
        WebTarget target = buildTarget.apply("suggest/gnd/").queryParam("searchterm", term).queryParam("type",
            TYPE_PLACE);
        JsonArray data = target.request(MediaType.APPLICATION_JSON).get(JsonArray.class);

        if (data.size() == 4) {
            JsonArray names = data.getJsonArray(1);
            JsonArray types = data.getJsonArray(2);
            JsonArray uris = data.getJsonArray(3);

            return IntStream.range(0, names.size())
                .filter(i -> ((JsonString) types.get(i)).getString().contains(TYPE_PLACE)).mapToObj(i -> {
                    Place place = new Place();
                    place.setName(((JsonString) names.get(i)).getString().trim());
                    Matcher m = PATTERN_BASE_URI.matcher(((JsonString) uris.get(i)).getChars());
                    if (m.find()) {
                        place.setMappedIds(
                            Stream.of(new IdType(Scheme.get("gnd"), m.group(1).trim())).collect(Collectors.toList()));
                    }

                    return place;
                }).collect(Collectors.toList());
        }
        return null;
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
     * @see org.mycore.lookup.api.service.LookupService#place(java.lang.String)
     */
    @Override
    public Place place(IdType idType) throws UnsupportedOperationException {
        Place p = RDFMapper.map(idType.getScheme().buildAuthorityURI(idType.getId()), Place.class);

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

}
