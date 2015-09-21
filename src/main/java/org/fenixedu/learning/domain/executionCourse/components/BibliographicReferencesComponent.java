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
package org.fenixedu.learning.domain.executionCourse.components;

import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;
import org.fenixedu.learning.domain.executionCourse.BibliographicReference;
import org.fenixedu.learning.domain.executionCourse.ExecutionCourseSite;

@ComponentType(name = "bibliographicReferences", description = "Bibliographic References for an Execution Course")
public class BibliographicReferencesComponent extends BaseExecutionCourseComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourseSite site = ((ExecutionCourseSite) page.getSite());
        globalContext.put("mainReferences", mainReferences(site));
        globalContext.put("secondaryReferences", secundaryReferences(site));
    }

    public List<BibliographicReference> mainReferences(ExecutionCourseSite executionCourse) {
        return executionCourse.getAssociatedBibliographicReferencesSet().stream().filter(b -> !b.isOptional())
                .sorted(BibliographicReference.COMPARATOR_BY_ORDER).collect(Collectors.toList());
    }

    public List<BibliographicReference> secundaryReferences(ExecutionCourseSite executionCourse) {
        return executionCourse.getAssociatedBibliographicReferencesSet().stream().filter(b -> b.isOptional())
                .sorted(BibliographicReference.COMPARATOR_BY_ORDER).collect(Collectors.toList());
    }

}
