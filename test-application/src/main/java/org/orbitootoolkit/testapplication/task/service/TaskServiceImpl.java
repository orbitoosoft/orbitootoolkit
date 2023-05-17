/*-
 * ========================LICENSE_START=================================
 * orbitoo-toolkit-test-application
 * %%
 * Copyright (C) 2023 orbitoo-soft
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * =========================LICENSE_END==================================
 */
package org.orbitootoolkit.testapplication.task.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.orbitootoolkit.core.api.DomainService;
import org.orbitootoolkit.core.api.TaggedValue;
import org.orbitootoolkit.testapplication.task.api.IssueService;
import org.orbitootoolkit.testapplication.task.model.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaskServiceImpl {
    @Retention(RetentionPolicy.RUNTIME)
    @Bean
    @DomainService( //
            servicePointName = "issueServicePoint", subjectClass = Issue.class, //
            subjectTaggedValues = @TaggedValue(tag = "type", value = "TASK") //
    )
    public static @interface State {
        @AliasFor(annotation = DomainService.class, attribute = "additionalTaggedValues")
        public TaggedValue[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Autowired
    @Qualifier
    @Lazy
    public static @interface Ref {
        @AliasFor(annotation = Qualifier.class, attribute = "value")
        public String value();
    }

    //

    @State(@TaggedValue(tag = "state", value = "OPENED"))
    public IssueService getTaskOpenedService(@Ref("getTaskInProgressService") IssueService taskInProgressService) {
        return new IssueService() {
            @Override
            public void entryState(Issue issue) {
                issue.setState("OPENED");
            }

            @Override
            public void issueImplementationStarted(Issue issue) {
                log.info("issueImplementationStarted: " + issue.getId());
                taskInProgressService.entryState(issue);
            }
        };
    }

    @State(@TaggedValue(tag = "state", value = "IN-PROGRESS"))
    public IssueService getTaskInProgressService(@Ref("getTaskInTestService") IssueService taskInTestService) {
        return new IssueService() {
            @Override
            public void entryState(Issue issue) {
                issue.setState("IN-PROGRESS");
            }

            @Override
            public void issueImplementationFinished(Issue issue) {
                log.info("issueImplementationFinished: " + issue.getId());
                taskInTestService.entryState(issue);
            }
        };
    }

    @State(@TaggedValue(tag = "state", value = "IN-TEST"))
    public IssueService getTaskInTestService( //
            @Ref("getTaskOpenedService") IssueService taskOpenedService, //
            @Ref("getTaskClosedService") IssueService taskClosedService //
    ) {
        return new IssueService() {
            @Override
            public void entryState(Issue issue) {
                issue.setState("IN-TEST");
            }

            @Override
            public void issueTested(Issue issue, boolean testPassed) {
                log.info("issueTested: " + issue.getId() + ", " + testPassed);
                if (testPassed) {
                    taskClosedService.entryState(issue);
                } else {
                    taskOpenedService.entryState(issue);
                }
            }
        };
    }

    @State(@TaggedValue(tag = "state", value = "CLOSED"))
    public IssueService getTaskClosedService() {
        return issue -> issue.setState("CLOSED");
    }
}
