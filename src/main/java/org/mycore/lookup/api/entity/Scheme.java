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
package org.mycore.lookup.api.entity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.mycore.lookup.common.config.ConfigurationDir;
import org.mycore.lookup.util.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "scheme")
public class Scheme {

    private static Schemes SCHEMES;

    private String id;

    private String[] alias;

    private String name;

    private String description;

    private String authorityURI;

    private RDF rdf;

    static {
        SCHEMES = new EntityFactory<Schemes>(Schemes.class)
            .fromXML(ConfigurationDir.getConfigResource("schemes.xml"));
    }

    /**
     * Returns all supported {@link Scheme}s.
     * 
     * @return a list of {@link Scheme}
     */
    public static List<Scheme> getAll() {
        return SCHEMES.getSchemes();
    }

    /**
     * Returns a {@link Scheme} for given scheme-id.
     * 
     * @param id the scheme id
     * @return the {@link Scheme}
     */
    public static Scheme get(final String id) {
        return SCHEMES.get(id);
    }

    /**
     * Extracts the {@link IdType} from given {@link URI}.
     * 
     * @param uri the {@link URI}
     * @return the {@link IdType}
     */
    public static IdType extractIdTypeFromURI(URI uri) {
        String uriStr = uri.toString();
        return getAll().stream().map(sh -> Stream
            .of(
                sh.getAuthorityURI() != null ? Pattern.compile(sh.getAuthorityURI().replace("{$id}", "(.+)"))
                    : null,
                sh.getRdfURI() != null ? Pattern.compile(sh.getRdfURI().replace("{$id}", "(.+)")) : null)
            .filter(p -> p != null).map(p -> {
                Matcher m = p.matcher(uriStr);
                return m.find() ? new IdType(sh, m.group(1)) : null;
            })
            .filter(id -> id != null).findFirst().orElse(null)).filter(id -> id != null).findFirst().orElse(null);
    }

    protected Scheme() {
    }

    /**
     * @param id
     * @param name
     * @param description
     * @param authorityUri
     */
    public Scheme(String id, String name, String description, String authorityUri) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.authorityURI = authorityUri;
    }

    /**
     * @return the id
     */
    @XmlAttribute
    public String getId() {
        return id;
    }

    /**
     * @return the alias
     */
    @XmlElement(name = "alias")
    @XmlList
    public String[] getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String[] alias) {
        this.alias = alias;
    }

    /**
     * @param id the id to set
     */
    public Scheme setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the name
     */
    @XmlElement
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public Scheme setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the description
     */
    @XmlElement
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public Scheme setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return the authorityURI
     */
    @XmlAttribute(name = "authorityURI")
    public String getAuthorityURI() {
        return authorityURI;
    }

    /**
     * @param authorityURI the authorityURI to set
     */
    public Scheme setAuthorityURI(String authorityURI) {
        this.authorityURI = authorityURI;
        return this;
    }

    @XmlElement(name = "rdf")
    protected RDF getRdf() {
        return this.rdf;
    }

    protected void setRdf(RDF rdf) {
        this.rdf = rdf;
    }

    /**
     * @return the rdfURI
     */
    @XmlTransient
    public String getRdfURI() {
        return rdf != null ? rdf.getUri() : null;
    }

    /**
     * @param rdfURI the rdfURI to set
     */
    public void setRdfURI(String rdfURI) {
        if (this.rdf == null) {
            this.rdf = new RDF();
        }
        this.rdf.setUri(rdfURI);
    }

    /**
     * @return the rdf type
     */
    @XmlTransient
    public String getRdfType() {
        return rdf != null ? rdf.getType() : null;
    }

    /**
     * @param rdfType the rdfType to set
     */
    public void setRdfType(String rdfType) {
        if (this.rdf == null) {
            this.rdf = new RDF();
        }
        this.rdf.setType(rdfType);
    }

    public URI buildAuthorityURI(String id) {
        try {
            return new URI(authorityURI.replace("{$id}", id));
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public URI buildRdfURI(String id) {
        try {
            return new URI(rdf.getUri().replace("{$id}", id));
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Scheme [");
        if (id != null) {
            builder.append("id=");
            builder.append(id);
            builder.append(", ");
        }
        if (name != null) {
            builder.append("name=");
            builder.append(name);
            builder.append(", ");
        }
        if (description != null) {
            builder.append("description=");
            builder.append(description);
            builder.append(", ");
        }
        if (authorityURI != null) {
            builder.append("authorityUri=");
            builder.append(authorityURI);
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * A container of multiple {@link Scheme}s.
     *  
     * @author Ren\u00E9 Adler (eagle)
     *
     */
    @XmlRootElement(name = "schemes")
    protected static class Schemes {

        private List<Scheme> schemes = new ArrayList<>();

        /**
         * @return the schemes
         */
        @XmlElement(name = "scheme")
        public List<Scheme> getSchemes() {
            return schemes;
        }

        /**
         * @param schemes the schemes to set
         */
        protected void setSchemes(List<Scheme> schemes) {
            this.schemes = schemes;
        }

        public Scheme get(String id) {
            return schemes.stream()
                .filter(s -> s.getId().equalsIgnoreCase(id)
                    || s.getAlias() != null && Arrays.stream(s.getAlias()).anyMatch(a -> a.equalsIgnoreCase(id)))
                .findFirst().orElse(null);
        }

        public void add(Scheme scheme) {
            schemes.add(scheme);
        }
    }

    @XmlRootElement(name = "rdf")
    protected static class RDF {

        private String uri;

        private String type;

        /**
         * @return the uri
         */
        @XmlAttribute
        public String getUri() {
            return uri;
        }

        /**
         * @param uri the uri to set
         */
        public void setUri(String uri) {
            this.uri = uri;
        }

        /**
         * @return the type
         */
        @XmlAttribute
        public String getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
        }

    }
}
