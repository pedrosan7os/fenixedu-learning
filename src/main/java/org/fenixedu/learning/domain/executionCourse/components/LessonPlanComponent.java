package org.fenixedu.learning.domain.executionCourse.components;

import com.google.common.collect.Maps;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.LessonPlanning;
import org.fenixedu.academic.domain.ShiftType;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.CMSComponent;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.learning.domain.executionCourse.ExecutionCourseSite;
import org.fenixedu.cms.rendering.TemplateContext;

import java.util.List;
import java.util.Map;

@ComponentType(name = "LessonsPlanning", description = "Lessons planing for an Execution Course")
public class LessonPlanComponent implements CMSComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        Map<ShiftType, List<LessonPlanning>> lessonPlanningsMap = Maps.newHashMap();
        for (ShiftType shiftType : executionCourse.getShiftTypes()) {
            List<LessonPlanning> lessonPlanningsOrderedByOrder = executionCourse.getLessonPlanningsOrderedByOrder(shiftType);
            if (!lessonPlanningsOrderedByOrder.isEmpty()) {
                lessonPlanningsMap.put(shiftType, lessonPlanningsOrderedByOrder);
            }
        }
        globalContext.put("lessonPlanningsMap", lessonPlanningsMap);
    }

}
