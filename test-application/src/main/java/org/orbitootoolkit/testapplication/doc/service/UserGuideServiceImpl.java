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
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserGuideServiceImpl {
    @Retention(RetentionPolicy.RUNTIME)
    @Bean
    @DomainService( //
            servicePointName = "documentServicePoint", subjectClass = Document.class, //
            subjectTaggedValues = @TaggedValue(tag = "type", value = "USER_GUIDE") //
    )
    public static @interface UserGuideState {
        @AliasFor(annotation = DomainService.class, attribute = "additionalTaggedValues")
        public TaggedValue[] value();
    }

    //

    @UserGuideState(@TaggedValue(tag = "state", value = "REQUESTED"))
    public DocumentService stateUserGuideRequested() {
        return new DocumentService() {
            @Override
            public void createDocument(Document document, String documentUri) {
                log.info("creating document: " + document);
                document.setDocumentUri(documentUri);
                document.setState(DocumentState.CREATED);
            }
        };
    }

    @UserGuideState(@TaggedValue(tag = "state", value = "CREATED"))
    public DocumentService stateUserGuideCreated() {
        return new DocumentService() {
            @Override
            public boolean approveDocument(Document document) {
                log.info("approving document: " + document);
                document.setState(DocumentState.APPROVED);
                return true;
            }
        };
    }

    @UserGuideState(@TaggedValue(tag = "state", value = "APPROVED"))
    public DocumentService stateUserGuideApproved() {
        return new DocumentService() {
        };
    }
}
