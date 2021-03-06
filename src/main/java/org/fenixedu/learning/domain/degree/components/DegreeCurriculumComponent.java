package org.fenixedu.learning.domain.degree.components;

import static java.util.stream.Collectors.*;
import static org.fenixedu.academic.domain.ExecutionYear.readCurrentExecutionYear;
import static pt.ist.fenixframework.FenixFramework.getDomainObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.curricularPeriod.CurricularPeriod;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.academic.domain.degreeStructure.CourseGroup;
import org.fenixedu.academic.util.CurricularPeriodLabelFormatter;
import org.fenixedu.academic.util.CurricularRuleLabelFormatter;

import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.domain.wraps.Wrap;
import org.fenixedu.cms.rendering.TemplateContext;
import org.fenixedu.commons.i18n.LocalizedString;

/**
 * Created by borgez on 10/10/14.
 */
@ComponentType(name = "Degree Curriculum", description = "Curriculum for a degree")
public class DegreeCurriculumComponent extends DegreeSiteComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Degree degree = degree(page);
        ExecutionYear selectedYear = selectedYear(globalContext.getRequestContext(), degree);
        globalContext.put("courseGroups", courseGroups(degree, selectedYear));
        globalContext.put("allCurricularCourses", allCurricularCourses(courseGroups(degree, selectedYear).collect(toSet())));

        globalContext.put("coursesByCurricularSemester", coursesByCurricularSemester(degree, selectedYear));
        globalContext.put("years", degree.getDegreeCurricularPlansExecutionYears());
        globalContext.put("selectedYear", selectedYear);
    }

    SortedMap<CurricularPeriod, Set<CurricularCourseWrap>> coursesByCurricularSemester(Degree degree, ExecutionYear year) {
        return allCurricularCourses(courseGroups(degree, year).collect(toSet())).collect(
                groupingBy(CurricularCourseWrap::getCurricularPeriod, TreeMap::new, toCollection(TreeSet::new)));
    }

    Stream<CurricularCourseWrap> allCurricularCourses(Collection<CourseGroupWrap> fathers) {
        Stream<CurricularCourseWrap> childrenCall =
                fathers.stream().flatMap(father -> allCurricularCourses(father.getCourseGroups().collect(toSet())));
        return Stream.concat(fathers.stream().flatMap(CourseGroupWrap::getCurricularCourses), childrenCall);
    }

    Stream<CourseGroupWrap> courseGroups(Degree degree, ExecutionYear year) {
        return degree.getDegreeCurricularPlansForYear(year).stream().filter(plan -> plan.isApproved() && plan.isActive())
                .map(plan -> new CourseGroupWrap(null, plan.getRoot(), year.getFirstExecutionPeriod()));
    }

    ExecutionYear selectedYear(String[] requestContext, Degree degree) {
        if (requestContext.length > 1) {
            return getDomainObject(requestContext[1]);
        } else {
            return Optional.ofNullable(degree.getLastActiveDegreeCurricularPlan().getLastExecutionYear()).orElse(
                    readCurrentExecutionYear());
        }
    }

    private class CourseGroupWrap extends Wrap {

        private final ExecutionSemester executionInterval;
        private final CourseGroup courseGroup;
        private final Context previous;

        public CourseGroupWrap(Context previous, CourseGroup courseGroup, ExecutionSemester executionInterval) {
            this.executionInterval = executionInterval;
            this.courseGroup = courseGroup;
            this.previous = previous;
        }

        public boolean hasCurricularCourses() {
            return courseGroup.hasAnyChildContextWithCurricularCourse();
        }

        public LocalizedString getName() {
            return courseGroup.getNameI18N().toLocalizedString();
        }

        public Stream<String> getRules() {
            return courseGroup.getVisibleCurricularRules(executionInterval).stream()
                    .filter(rule -> rule.appliesToContext(previous)).map(rule -> CurricularRuleLabelFormatter.getLabel(rule));
        }

        public Stream<CurricularCourseWrap> getCurricularCourses() {
            return courseGroup.getSortedOpenChildContextsWithCurricularCourses(executionInterval.getExecutionYear()).stream()
                    .map(context -> new CurricularCourseWrap(context, executionInterval));
        }

        public Stream<CourseGroupWrap> getCourseGroups() {
            return courseGroup
                    .getSortedOpenChildContextsWithCourseGroups(executionInterval)
                    .stream()
                    .map(context -> new CourseGroupWrap(context, (CourseGroup) context.getChildDegreeModule(), executionInterval));
        }
    }

    private class CurricularCourseWrap extends Wrap implements Comparable<CurricularCourseWrap> {
        private final Context context;
        private final ExecutionSemester executionInterval;
        private final CurricularCourse curricularCourse;

        public CurricularCourseWrap(Context context, ExecutionSemester executionInterval) {
            this.context = context;
            this.curricularCourse = (CurricularCourse) context.getChildDegreeModule();
            this.executionInterval = executionInterval;
        }

        public boolean isOptional() {
            return curricularCourse.isOptional();
        }

        public LocalizedString getName() {
            return curricularCourse.getNameI18N(executionInterval).toLocalizedString();
        }

        public String getUrl() {
            return "curricular-course/" + curricularCourse.getExternalId();
        }

        public String getContextInformation() {
            return CurricularPeriodLabelFormatter.getFullLabel(context.getCurricularPeriod(), true);
        }

        public boolean isSemestrial() {
            return curricularCourse.isSemestrial(executionInterval.getExecutionYear());
        }

        public boolean hasRegime() {
            return !isOptional() && curricularCourse.hasRegime(executionInterval.getExecutionYear());
        }

        public String getRegime() {
            return curricularCourse.getRegime(executionInterval).getLocalizedName();
        }

        public String getRegimeAcronym() {
            return curricularCourse.getRegime(executionInterval).getAcronym();
        }

        public String getContactLoad() {
            Double load = curricularCourse.getContactLoad(context.getCurricularPeriod(), executionInterval);
            return new BigDecimal(load).setScale(2, RoundingMode.HALF_EVEN).toPlainString();
        }

        public String getAutonomousWorkHours() {
            return curricularCourse.getAutonomousWorkHours(context.getCurricularPeriod(), executionInterval).toString();
        }

        public String getTotalLoad() {
            return curricularCourse.getTotalLoad(context.getCurricularPeriod(), executionInterval).toString();
        }

        public String getECTS() {
            return curricularCourse.getEctsCredits(context.getCurricularPeriod(), executionInterval).toString();
        }

        public boolean hasCompentenceCourse() {
            return curricularCourse.getCompetenceCourse() != null;
        }

        public Stream<String> getRules() {
            return curricularCourse.getVisibleCurricularRules(executionInterval).stream()
                    .filter(rule -> rule.appliesToContext(context)).map(rule -> CurricularRuleLabelFormatter.getLabel(rule));
        }

        public CurricularPeriod getCurricularPeriod() {
            return context.getCurricularPeriod();
        }

        @Override
        public int compareTo(CurricularCourseWrap o) {
            return getName().compareTo(o.getName());
        }
    }

}
