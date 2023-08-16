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
package org.orbitootoolkit.testapplication.doc.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.orbitootoolkit.core.api.DomainService;
import org.orbitootoolkit.core.api.TaggedValue;
import org.orbitootoolkit.testapplication.doc.api.DocumentService;
import org.orbitootoolkit.testapplication.doc.model.Document;
import org.orbitootoolkit.testapplication.doc.model.DocumentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserGuideService {
    @Retention(RetentionPolicy.RUNTIME)
    @Bean
    @DomainService( //
            servicePointName = "documentApi", subjectClass = Document.class, //
            subjectTaggedValues = @TaggedValue(tag = "type", value = "USER_GUIDE") //
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

    @State(@TaggedValue(tag = "state", value = "REQUESTED"))
    public DocumentService getStateUserGuideRequested(@Ref("getStateUserGuideCreated") DocumentService stateCreated) {
        return new DocumentService() {
            @Override
            public void entryState(Document document) {
                log.info("document requested: " + document.toString());
                document.setState(DocumentState.REQUESTED);
            }

            @Override
            public void createDocument(Document document) {
                stateCreated.entryState(document);
            }
        };
    }

    @State(@TaggedValue(tag = "state", value = "CREATED"))
    public DocumentService getStateUserGuideCreated(@Ref("getStateUserGuideApproved") DocumentService stateApproved) {
        return new DocumentService() {
            @Override
            public void entryState(Document document) {
                log.info("document created: " + document.toString());
                document.setState(DocumentState.CREATED);
            }

            @Override
            public boolean approveDocument(Document document) {
                stateApproved.entryState(document);
                return true;
            }
        };
    }

    @State(@TaggedValue(tag = "state", value = "APPROVED"))
    public DocumentService getStateUserGuideApproved() {
        return document -> {
            log.info("document approved: " + document.toString());
            document.setState(DocumentState.APPROVED);
        };
    }
}
