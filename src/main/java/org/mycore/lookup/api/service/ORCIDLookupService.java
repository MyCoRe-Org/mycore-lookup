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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.mycore.lookup.api.entity.Corporate;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Person;
import org.mycore.lookup.api.entity.Place;
import org.mycore.lookup.api.entity.Scheme;
import org.mycore.lookup.api.service.annotation.Service;
import org.mycore.lookup.frontend.provider.GenericExceptionMapper;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Service(type = LookupService.class)
@Priority(0)
public class ORCIDLookupService extends LookupService {

    private static final String REMOTE_URL = "https://pub.orcid.org";

    private static final String REMOTE_PATH = "v2.1";

    private Function<String, WebTarget> buildTarget = (method) -> {
        ClientConfig config = new ClientConfig();
        config.register(MoxyJsonFeature.class);
        config.register(GenericExceptionMapper.class);

        return ClientBuilder.newClient(config).target(REMOTE_URL).path(REMOTE_PATH + "/" + method);
    };

    public static String encodeURIComponent(String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                .replaceAll("\\+", "%20")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public ORCIDLookupService() {
        super();
        this.supportedSchemes.add(Scheme.get("orcid"));
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
        String options = "{!edismax qf=\"given-and-family-names^50.0 family-name^10.0 given-names^5.0 credit-name^10.0 other-names^5.0 text^1.0\" pf=\"given-and-family-names^50.0\" mm=1}";
        WebTarget target = buildTarget.apply("search").queryParam("q", encodeURIComponent(options + term))
            .queryParam("start", 0)
            .queryParam("rows", 100);
        Response response = target.request(MediaType.APPLICATION_XML).get();

        if (response.getStatus() == 200) {
            ORCIDSearch res = response.readEntity(ORCIDSearch.class);

            return Optional.ofNullable(res.result)
                .map(result -> result.parallelStream()
                    .map(oi -> person(new IdType(Scheme.get("orcid"), oi.identifier.path)))
                    .collect(Collectors.toList()))
                .orElse(null);
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
     * @see org.mycore.lookup.api.service.LookupService#corporate(org.mycore.lookup.api.entity.IdType)
     */
    @Override
    public Corporate corporate(IdType idType) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#person(org.mycore.lookup.api.entity.IdType)
     */
    @Override
    public Person person(IdType idType) throws UnsupportedOperationException {
        WebTarget target = buildTarget.apply(idType.getId() + "/person");
        Response response = target.request(MediaType.APPLICATION_XML).get();

        if (response.getStatus() == 200) {
            ORCIDPerson op = response.readEntity(ORCIDPerson.class);
            Person p = op.toPerson();

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

        return null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.service.LookupService#place(org.mycore.lookup.api.entity.IdType)
     */
    @Override
    public Place place(IdType idType) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @XmlRootElement(name = "search", namespace = "http://www.orcid.org/ns/search")
    private static class ORCIDSearch {

        @XmlElement(name = "result", namespace = "http://www.orcid.org/ns/search")
        public List<ORCIDResult> result;

    }

    @XmlRootElement(name = "result", namespace = "http://www.orcid.org/ns/search")
    private static class ORCIDResult {

        @XmlElement(name = "orcid-identifier", namespace = "http://www.orcid.org/ns/common")
        public ORCIDIdentifier identifier;
    }

    @XmlRootElement(name = "orcid-identifier", namespace = "http://www.orcid.org/ns/common")
    private static class ORCIDIdentifier {

        @XmlElement(name = "uri", namespace = "http://www.orcid.org/ns/common")
        public String uri;

        @XmlElement(name = "path", namespace = "http://www.orcid.org/ns/common")
        public String path;

        @XmlElement(name = "host", namespace = "http://www.orcid.org/ns/common")
        public String host;

    }

    @XmlRootElement(name = "person", namespace = "http://www.orcid.org/ns/person")
    private static class ORCIDPerson {

        @XmlElement(name = "name", namespace = "http://www.orcid.org/ns/person")
        public ORCIDPersonName name;

        @XmlElementWrapper(name = "other-names", namespace = "http://www.orcid.org/ns/other-name")
        @XmlElement(name = "other-name", namespace = "http://www.orcid.org/ns/other-name")
        public List<ORCIDPersonOtherName> otherNames;

        @XmlElementWrapper(name = "external-identifiers", namespace = "http://www.orcid.org/ns/external-identifier")
        @XmlElement(name = "external-identifier", namespace = "http://www.orcid.org/ns/external-identifier")
        public List<ORCIDExternalIdentifier> externalIdentifiers;

        public Person toPerson() {
            Person p = new Person();

            p.setGivenName(this.name.givenNames);
            p.setFamilyName(this.name.familyName);

            p.setAlternateNames(this.otherNames.stream().map(on -> on.content).collect(Collectors.toList()));

            p.setMappedIds(this.externalIdentifiers.stream().filter(ei -> Scheme.get(ei.type) != null)
                .map(ei -> new IdType(Scheme.get(ei.type), ei.value)).collect(Collectors.toList()));

            return p;
        }

    }

    @XmlRootElement(name = "name", namespace = "http://www.orcid.org/ns/person")
    private static class ORCIDPersonName {

        @XmlElement(name = "given-names", namespace = "http://www.orcid.org/ns/personal-details")
        public String givenNames;

        @XmlElement(name = "family-name", namespace = "http://www.orcid.org/ns/personal-details")
        public String familyName;

    }

    @XmlRootElement(name = "other-name", namespace = "http://www.orcid.org/ns/other-name")
    private static class ORCIDPersonOtherName {

        @XmlElement(name = "content", namespace = "http://www.orcid.org/ns/other-name")
        public String content;

    }

    @XmlRootElement(name = "external-identifier", namespace = "http://www.orcid.org/ns/external-identifier")
    private static class ORCIDExternalIdentifier {

        @XmlElement(name = "external-id-type", namespace = "http://www.orcid.org/ns/common")
        public String type;

        @XmlElement(name = "external-id-value", namespace = "http://www.orcid.org/ns/common")
        public String value;

        @XmlElement(name = "external-id-url", namespace = "http://www.orcid.org/ns/common")
        public String url;

    }

}
