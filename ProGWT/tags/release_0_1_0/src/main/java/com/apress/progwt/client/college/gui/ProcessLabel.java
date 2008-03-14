/*
 * Copyright 2008 Jeff Dwyer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.apress.progwt.client.college.gui;

import java.util.Date;

import com.apress.progwt.client.college.gui.timeline.TimelineController;
import com.apress.progwt.client.domain.ProcessType;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ProcessLabel extends Label {

    private ProcessType processType;

    public ProcessLabel(final ProcessType processType,
            final TimelineController controller, final Date date) {
        super(processType.getName());

        this.processType = processType;
        setStylePrimaryName("ProcessLabel");
        addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                controller.addProcess(processType, date);
            }
        });
    }

    public ProcessType getProcessType() {
        return processType;
    }

}
