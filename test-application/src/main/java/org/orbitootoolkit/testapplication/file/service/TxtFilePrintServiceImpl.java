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
package org.orbitootoolkit.testapplication.file.service;

import org.orbitootoolkit.core.api.DomainService;
import org.orbitootoolkit.testapplication.file.api.FilePrintService;
import org.orbitootoolkit.testapplication.file.model.File;
import org.orbitootoolkit.testapplication.file.model.TxtFile;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@DomainService(servicePointName = "filePrintServicePoint", subjectClass = TxtFile.class)
public class TxtFilePrintServiceImpl implements FilePrintService {
    @Override
    public void print(File file) {
        log.info("printing file: " + file);
    }
}
