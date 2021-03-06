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

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

import org.mycore.lookup.backend.index.adapter.FieldAdapter;

/**
 * {@code FieldAdapter} mapping any JSR-310 {@code TemporalAccessor} to string using provided {@code DateTimeFormatter}

 * @author Ren\u00E9 Adler (eagle)
 * 
 * @see java.time.temporal.TemporalAccessor
 * @see java.time.format.DateTimeFormatter
 */
public class DateTemporalAccessorFieldAdapter extends FieldAdapter<String, TemporalAccessor> {

    private final List<String> DATE_PATTERNS = Arrays.asList("yyyy[[-MM][-dd]]", "[[dd.][MM.]]yyyy", "[[dd.][M.]]yyyy");

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public TemporalAccessor unmarshal(String v) {
        return v != null ? DATE_PATTERNS.stream().map(dp -> {
            try {
                return DateTimeFormatter.ofPattern(dp).parseBest(v, LocalDate::from,
                    YearMonth::from, Year::from);
            } catch (DateTimeParseException e) {
                return null;
            }
        }).filter(s -> s != null).findFirst().orElse(null) : null;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(TemporalAccessor v) {
        return v != null ? v.toString() : null;
    }

}
