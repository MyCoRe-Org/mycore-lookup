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
package org.mycore.lookup.api.entity.adapter;

import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Scheme;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class IdTypeAdapter extends XmlAdapter<Element, IdType> {

    private DocumentBuilder documentBuilder;

    private JAXBContext jaxbContext;

    public IdTypeAdapter() {
    }

    public IdTypeAdapter(JAXBContext jaxbContext) {
        this();
        this.jaxbContext = jaxbContext;
    }

    private DocumentBuilder getDocumentBuilder() throws Exception {
        // Lazy load the DocumentBuilder as it is not used for unmarshalling.
        if (null == documentBuilder) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            documentBuilder = dbf.newDocumentBuilder();
        }
        return documentBuilder;
    }

    private JAXBContext getJAXBContext(Class<?> type) throws Exception {
        if (null == jaxbContext) {
            // A JAXBContext was not set, so create a new one based  on the type.
            return JAXBContext.newInstance(type);
        }
        return jaxbContext;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public IdType unmarshal(Element v) throws Exception {
        if (null == v) {
            return null;
        }

        DOMSource source = new DOMSource(v);
        Unmarshaller unmarshaller = getJAXBContext(String.class).createUnmarshaller();
        JAXBElement<String> jaxbElement = (JAXBElement<String>) unmarshaller.unmarshal(source, String.class);

        IdType id = new IdType();
        id.setScheme(Scheme.get(v.getLocalName()));

        if (id.getScheme() == null)
            return null;

        id.setId(jaxbElement.getValue());
        return id;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public Element marshal(IdType v) throws Exception {
        if (null == v) {
            return null;
        }

        QName rootElement = new QName(v.getScheme().getId().toLowerCase(Locale.ROOT));
        JAXBElement<String> jaxbElement = new JAXBElement<String>(rootElement, String.class, v.getId());

        Document document = getDocumentBuilder().newDocument();
        Marshaller marshaller = getJAXBContext(String.class).createMarshaller();
        marshaller.marshal(jaxbElement, document);
        Element element = document.getDocumentElement();
        return element;
    }

}
