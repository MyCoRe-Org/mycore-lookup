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

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.mycore.lookup.api.entity.IdType;
import org.mycore.lookup.api.entity.Scheme;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MappedIdAdapter extends XmlAdapter<Element, List<IdType>> {

    private DocumentBuilder documentBuilder;

    private DocumentBuilder getDocumentBuilder() throws Exception {
        // Lazy load the DocumentBuilder as it is not used for unmarshalling.
        if (null == documentBuilder) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            documentBuilder = dbf.newDocumentBuilder();
        }
        return documentBuilder;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public List<IdType> unmarshal(Element v) throws Exception {
        if (null == v) {
            return null;
        }

        NodeList nodeList = v.getChildNodes();
        if (nodeList.getLength() == 0) {
            return null;
        }

        return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item)
            .peek(LogManager.getLogger()::info)
            .filter(node -> node.getNodeType() == Node.TEXT_NODE
                && Scheme.get(node.getParentNode().getLocalName()) != null)
            .map(node -> {
                IdType id = new IdType();
                id.setScheme(Scheme.get(node.getParentNode().getLocalName()));
                id.setId(node.getTextContent());
                return id;
            }).peek(LogManager.getLogger()::info).collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public Element marshal(List<IdType> v) throws Exception {
        if (null == v || v.isEmpty()) {
            return null;
        }

        Document document = getDocumentBuilder().newDocument();
        Element rootElement = document.createElement("mappedIds");
        document.appendChild(rootElement);
        v.forEach(id -> {
            Element elm = document.createElement(id.getScheme().getId().toLowerCase(Locale.ROOT));
            elm.setTextContent(id.getId());
            rootElement.appendChild(elm);
        });

        return rootElement;
    }

}
