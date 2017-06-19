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

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class DateAdapter extends RDFMappingAdapter<String, TemporalAccessor> {

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public TemporalAccessor unmarshal(String v) {
        TemporalAccessor ta = null;
        try {
            ta = DateTimeFormatter.ofPattern("yyyy[[-MM][-dd]]").parseBest(v, LocalDate::from,
                YearMonth::from, Year::from);
        } catch (DateTimeParseException e) {
            ta = DateTimeFormatter.ofPattern("[[dd.][MM.]]yyyy").parseBest(v, LocalDate::from,
                YearMonth::from, Year::from);
        }
        return v != null ? ta : null;
    }

    /* (non-Javadoc)
     * @see org.mycore.lookup.api.rdf.adapter.RDFMappingAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(TemporalAccessor v) {
        return v.toString();
    }

}
