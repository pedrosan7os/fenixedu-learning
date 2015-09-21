/**
 * Copyright © 2015 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Learning.
 *
 * FenixEdu Learning is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Learning is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Learning.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.learning.domain.degree;

import java.util.Objects;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.cms.domain.Category;
import org.fenixedu.cms.domain.wraps.Wrap;
import org.fenixedu.commons.i18n.LocalizedString;

public class DegreeSite extends DegreeSite_Base {

    public DegreeSite(String degreeId, LocalizedString name, LocalizedString description) {
        super();
        setDegreeId(degreeId);
        setName(name);
        setDescription(description);
        setBennu(Bennu.getInstance());
    }

    @Override
    public void delete() {
        this.setBennu(null);
        super.delete();
    }

    public Stream<Wrap> getCategoriesToShow() {
        return Stream.of(categoryForSlug("announcement")).filter(Objects::nonNull).map(Category::makeWrap);
    }

}
