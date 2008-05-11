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
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.apress.progwt.client.college.ServiceCache;
import com.apress.progwt.client.college.gui.ext.AlertDialog;
import com.apress.progwt.client.college.gui.ext.ContextMenu;
import com.apress.progwt.client.college.gui.ext.H2;
import com.apress.progwt.client.college.gui.timeline.ProcessTimeLineEntry;
import com.apress.progwt.client.college.gui.timeline.ProcessTimeLineObjFactory;
import com.apress.progwt.client.college.gui.timeline.ProcessTimeline;
import com.apress.progwt.client.college.gui.timeline.TimelineController;
import com.apress.progwt.client.domain.Application;
import com.apress.progwt.client.domain.ProcessType;
import com.apress.progwt.client.domain.ProcessValue;
import com.apress.progwt.client.domain.User;
import com.apress.progwt.client.domain.commands.SaveProcessCommand;
import com.apress.progwt.client.domain.commands.SiteCommand;
import com.apress.progwt.client.gui.timeline.TimeLineObj;
import com.apress.progwt.client.gui.timeline.TimeLineObjFactory;
import com.apress.progwt.client.rpc.StdAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A MyPageTab tab that display our users application information.
 * Specifically the process information.
 * 
 * @author Jeff Dwyer
 * 
 */
public class MyApplicationTimeline extends Composite implements
        MyPageTab, TimelineController {

    /**
     * Hold all Applications and set the timeline to display the chosen
     * applications.
     * 
     * @author Jeff Dwyer
     * 
     */
    private class AppList extends Composite {
        private VerticalPanel mainPanel;

        private Label selected;

        public AppList() {
            mainPanel = new VerticalPanel();
            initWidget(mainPanel);
            setStylePrimaryName("AppTimelineAppList");
        }

        public void load(User user) {
            lastUser = user;
            List<Application> applications = user.getSchoolRankings();

            mainPanel.clear();
            mainPanel.add(new H2("Applications"));

            for (final Application app : applications) {

                final Label schoolL = new Label(app.getSchool().getName());

                schoolL.setStylePrimaryName("ApplicationSelector");
                schoolL.addClickListener(new ClickListener() {
                    public void onClick(Widget sender) {

                        if (selected != null) {
                            selected.removeStyleDependentName("selected");
                        }
                        selected = schoolL;
                        selected.addStyleDependentName("selected");
                        showApplication(app);

                    }
                });

                mainPanel.add(schoolL);
            }
        }

    }

    /**
     * Hold all ProcessType and create new TLO's when they're chosen.
     * 
     * Make them draggable, so they can eb drgged onto the timeline, but
     * don't make them drggable if they'll be used in the context menu.
     * 
     * @author Jeff Dwyer
     * 
     */
    private class ProcessTypePanel extends Composite {
        private ClickListener allClicks;
        private Date date;
        private VerticalPanel mainPanel;

        public ProcessTypePanel() {
            this(new Date(), null);
        }

        public ProcessTypePanel(Date date, ClickListener allClicks) {
            this.date = date;
            this.allClicks = allClicks;
            mainPanel = new VerticalPanel();
            initWidget(mainPanel);

        }

        public void load(User user) {

            mainPanel.clear();

            mainPanel.add(new H2("Application Events"));

            List<ProcessType> processTypes = user.getProcessTypes();

            for (final ProcessType processType : processTypes) {

                ProcessLabel processLabel = new ProcessLabel(processType,
                        MyApplicationTimeline.this, date);

                if (allClicks != null) {
                    processLabel.addClickListener(allClicks);
                } else {
                    timeline.getDragController().makeDraggable(
                            processLabel);
                }
                mainPanel.add(processLabel);
            }
        }

    }

    private Application currentApplication;

    private User lastUser;

    private ProcessTypePanel processTypePanel;

    private AppList schoolPanel;

    // private User thisUser;
    private ServiceCache serviceCache;

    private ProcessTimeline timeline;

    private TimeLineObjFactory timelineObjFactory = new ProcessTimeLineObjFactory();

    public MyApplicationTimeline(ServiceCache serviceCache) {

        this.serviceCache = serviceCache;

        HorizontalPanel mainP = new HorizontalPanel();        
        VerticalPanel chooserP = new VerticalPanel();

        timeline = new ProcessTimeline(this);

        schoolPanel = new AppList();
        processTypePanel = new ProcessTypePanel();

        chooserP.add(schoolPanel);
        chooserP.add(processTypePanel);

        mainP.add(timeline);
        mainP.add(chooserP);

        initWidget(mainP);

    }

    /**
     * called from double click on the background, we know the date, but
     * need a processtype, use a context menu to get the type, which will
     * callback to addProcess(ProcessType,Date) when selected.
     */
    public void addProcess(Date date, int x, int y) {
        Log.debug("AddProcess(date only) " + date);

        final ContextMenu menu = new ContextMenu(x, y);

        ProcessTypePanel typePanel = new ProcessTypePanel(date,
                new ClickListener() {
                    public void onClick(Widget sender) {
                        menu.hide();
                    }
                });
        typePanel.load(lastUser);

        menu.clear();
        menu.setWidget(typePanel);
        menu.show();
    }

    public void addProcess(ProcessType processType, Date date) {
        Log.debug("AddProcess " + processType + " " + date);
        ProcessValue value = new ProcessValue();
        value.setDueDate(date);

        if (getCurrentApplication() != null) {
            ProcessValue existing = getCurrentApplication().getProcess()
                    .get(processType);

            if (existing != null) {
                AlertDialog
                        .alert("Sorry, that already exists for this application.");
                return;
            }
            saveProcess(processType, value);

            // add before callback returns is fine. We'll just overwrite
            // if they edit.
            timeline.addNew(getCurrentApplication(), processType, value);

        } else {
            AlertDialog.alert("Select an application");
        }

    }

    public void delete(Application application) {
        // TODO Auto-generated method stub

    }

    public Application getCurrentApplication() {
        return currentApplication;
    }

    public String getHistoryName() {
        return "MyApplications";
    }

    public TimeLineObjFactory getTimeLineObjFactory() {
        return timelineObjFactory;
    }

    public void load(User user) {

        timeline.load(user);
        schoolPanel.load(user);
        processTypePanel.load(user);

    }

    public void onTLOChange(TimeLineObj<?> tlo) {
        TimeLineObj<ProcessTimeLineEntry> tlopte = (TimeLineObj<ProcessTimeLineEntry>) tlo;

        SaveProcessCommand command = new SaveProcessCommand(
                getCurrentApplication(), tlopte.getObject()
                        .getProcessType(), tlopte.getObject()
                        .getProcessValue());

        serviceCache.executeCommand(command,
                new AsyncCallback<SiteCommand>() {

                    public void onFailure(Throwable caught) {
                        Log.error("fail");
                    }

                    public void onSuccess(SiteCommand result) {
                        Log.debug("Success");
                    }
                });
        serviceCache.executeCommand(command,
                new StdAsyncCallback<SiteCommand>("Update Process Date") {
                });

    }

    public void refresh() {
        // TODO Auto-generated method stub

    }

    public void saveProcess(ProcessType processType, ProcessValue value) {

        SaveProcessCommand command = new SaveProcessCommand(
                getCurrentApplication(), processType, value);

        serviceCache.executeCommand(command,
                new StdAsyncCallback<SiteCommand>("Save Process") {
                });

    }

    public void setCurrentApplication(Application currentApplication) {
        this.currentApplication = currentApplication;
    }

    public void setSelected(TimeLineObj<?> tlo) {

        timeline.showStatus((TimeLineObj<ProcessTimeLineEntry>) tlo);

    }

    protected void showApplication(Application app) {
        setCurrentApplication(app);
        timeline.showApplication(app);
    }
}
