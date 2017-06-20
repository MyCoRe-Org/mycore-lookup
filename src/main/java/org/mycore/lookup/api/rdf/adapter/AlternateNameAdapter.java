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
package org.mycore.lookup.api.rdf.adapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class AlternateNameAdapter extends RDFMappingAdapter<List<String>, List<String>> {

    private static final Pattern PATTERN_DISPLAY_FORM = Pattern
        .compile("([^,]+),\\s?([^,]+(?=,)|[a-z-A-Z\\s\\d\\.\\-]+(?!,))(?:,\\s|\\s?)(?:(\\d+)\\??\\-(\\d+)?\\??)?");

    public static String parse(String name) {
        Matcher m = PATTERN_DISPLAY_FORM.matcher(name);
        if (m.find()) {
            return m.group(1).trim() + ", " + m.group(2).trim();
        }
        return name;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public List<String> unmarshal(List<String> v) {
        return v.stream().map(AlternateNameAdapter::parse).map(s -> {
            try {
                if (s.contains(",")) {
                    return s.substring(0, s.lastIndexOf(",")).trim() + ", "
                        + s.substring(s.lastIndexOf(",") + 1).trim();
                }

                return s.substring(s.lastIndexOf(" ") + 1).trim() + ", "
                    + s.substring(0, s.lastIndexOf(" ")).trim();
            } catch (StringIndexOutOfBoundsException e) {
                return s;
            }
        }).filter(e -> e != null).collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#marshal(java.lang.Object)
     */
    @Override
    public List<String> marshal(List<String> v) {
        return v;
    }

}
