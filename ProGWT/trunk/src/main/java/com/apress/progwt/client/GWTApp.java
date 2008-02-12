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
package com.apress.progwt.client;

import com.allen_sauer.gwt.log.client.Log;
import com.apress.progwt.client.college.LoginService;
import com.apress.progwt.client.college.ServiceCache;
import com.apress.progwt.client.college.gui.status.StatusPanel;
import com.apress.progwt.client.consts.ConstHolder;
import com.apress.progwt.client.consts.images.Images;
import com.apress.progwt.client.rpc.StdAsyncCallback;
import com.apress.progwt.client.service.remote.GWTSchoolService;
import com.apress.progwt.client.service.remote.GWTSchoolServiceAsync;
import com.apress.progwt.client.service.remote.GWTUserService;
import com.apress.progwt.client.service.remote.GWTUserServiceAsync;
import com.google.gwt.core.client.GWT;
import com.google.gwt.gears.core.client.GearsException;
import com.google.gwt.gears.localserver.client.LocalServer;
import com.google.gwt.gears.localserver.client.ManagedResourceStore;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamReader;
import com.google.gwt.user.client.rpc.impl.RemoteServiceProxy;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GWTApp {

    private static final String MANIFEST_URL = "site/manifest.json";

    private static String getLoadID(int id) {
        return "gwt-slot-" + id;
    }

    private static String getPreLoadID(int id) {
        return "gwt-loading-" + id;
    }

    public static void show(int id, Widget panel) {
        RootPanel.get(getPreLoadID(id)).setVisible(false);
        RootPanel.get(getLoadID(id)).add(panel);
    }

    private LoginService loginService;
    private int pageID;

    private GWTSchoolServiceAsync schoolService;

    private ServiceCache serviceCache;

    private GWTUserServiceAsync userService;

    public GWTApp(int pageID) {
        this.pageID = pageID;

        // setup the StatusPanel. There will be just one DIV for this, no
        // matter how many apps we have in the same page.
        try {
            RootPanel status = RootPanel.get("gwt-status");
            if (status.getWidgetCount() == 0) {
                StatusPanel sp = new StatusPanel();
                status.add(sp);
                StdAsyncCallback.setManager(sp);
            }

        } catch (Exception e) {
            Log.error("Status Panel problem: ");
        }

        // create the Local Server after enough of a delay so that we try
        // to appear as lightweight as possible.
        new Timer() {
            @Override
            public void run() {
                try {
                    doLocalServer();
                } catch (Exception e) {
                    Log.warn("LocalServer exception: " + e);
                }
            }
        }.schedule(10000);

    }

    public Object deserialize(String serialized) {

        ClientSerializationStreamReader c;
        Log.debug("Try to deserialize: " + serialized);
        try {
            c = getBootstrapService().createStreamReader(serialized);

            Object o = c.readObject();
            return o;
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private void doLocalServer() throws GearsException {
        LocalServer localServer = new LocalServer();

        final ManagedResourceStore managedResourceStore = localServer
                .createManagedResourceStore("ToCollege.net");
        try {
            managedResourceStore.setManifestURL(Interactive
                    .getRelativeURL(MANIFEST_URL));
        } catch (Exception e) {
            // expected in hosted mode. catches JS exception from setting
            // 8080 when running on 8888
            throw new GearsException(e.getMessage());
        }

        new Timer() {
            public void run() {
                switch (managedResourceStore.getUpdateStatus()) {
                case ManagedResourceStore.UPDATE_OK:
                    Log.info("UPDATE_OK "
                            + managedResourceStore.getCurrentVersion());
                    cancel();
                    break;
                case ManagedResourceStore.UPDATE_CHECKING:
                    Log.debug("Checking "
                            + managedResourceStore.getCurrentVersion());
                    break;
                case ManagedResourceStore.UPDATE_DOWNLOADING:
                    Log.debug("Downloading "
                            + managedResourceStore.getCurrentVersion());
                    break;
                case ManagedResourceStore.UPDATE_FAILED:
                    Log.warn("Fail "
                            + managedResourceStore.getCurrentVersion());
                    Log.warn(managedResourceStore.getLastErrorMessage());
                    cancel();
                    break;
                }

            }
        }.scheduleRepeating(2000);
        managedResourceStore.checkForUpdate();
    }

    /**
     * get the Object that has been serialized under the JavaScript var
     * name "serialized"
     * 
     * @return
     */
    protected Object getBootstrapped() {
        return getBootstrapped("serialized");
    }

    /**
     * 
     * Remember, the RemoteServiceProxy that you use must have a method
     * that returns the type that you wish to serialize. Otherwise, the
     * deserializer will not be created.
     * 
     * Cast the service into a RemoteServiceProxy, grab the stream reader
     * and deserialize.
     * 
     * @param name
     * @return
     */
    private Object getBootstrapped(String name) {
        String serialized = getParam(name);
        if (serialized == null) {
            Log.warn("No param " + name);
            return null;
        }

        try {
            return deserialize(serialized);
        } catch (Exception e) {
            Log.error("Bootstrap " + name + " Problem ", e);
            return null;
        }
    }

    private RemoteServiceProxy getBootstrapService() {
        return (RemoteServiceProxy) getSchoolService();
    }

    protected String getLoadID() {
        return getLoadID(pageID);
    }

    public LoginService getLoginService() {
        return loginService;
    }

    protected String getParam(String string) {
        try {
            Dictionary dictionary = Dictionary.getDictionary("Vars");
            return dictionary.get(string + "_" + pageID);
        } catch (Exception e) {
            Log.info("Couldn't find param: " + string);
            return null;
        }

    }

    protected String getPreLoadID() {
        return getPreLoadID(pageID);
    }

    public GWTSchoolServiceAsync getSchoolService() {
        return schoolService;
    }

    public ServiceCache getServiceCache() {
        return serviceCache;
    }

    public GWTUserServiceAsync getUserService() {
        return userService;
    }

    // this doesn't work. serialization strings are different depending on
    // which way they go
    // School s = new School();
    // String ser = serialize(s);
    // School e = (School) deserialize(ser);
    //
    // public String serialize(Object o) {
    // try {
    // ClientSerializationStreamWriter w = getBootstrapService()
    // .createStreamWriter();
    // w.writeObject(o);
    // Log.debug("Serialized: " + o + "\nto\n"
    // + w.toString());
    // return w.toString();
    // } catch (SerializationException e) {
    // throw new RuntimeException(e);
    // }
    // }

    protected void initConstants() {
        ConstHolder.images = (Images) GWT.create(Images.class);
    }

    /**
     * call initServices if your GWTApp would like the asynchronous
     * services to be setup
     */
    protected void initServices() {

        schoolService = (GWTSchoolServiceAsync) GWT
                .create(GWTSchoolService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) schoolService;

        String pre = Interactive.getRelativeURL("service/");

        endpoint.setServiceEntryPoint(pre + "schoolService");

        userService = (GWTUserServiceAsync) GWT
                .create(GWTUserService.class);
        ServiceDefTarget endpointUser = (ServiceDefTarget) userService;
        endpointUser.setServiceEntryPoint(pre + "userService");

        if (schoolService == null || userService == null) {
            Log.error("Service was null.");
        }

        serviceCache = new ServiceCache(this);
        loginService = new LoginService(serviceCache);

    }

    protected void loadError(Exception e) {

        Log.error("e: " + e);

        e.printStackTrace();

        VerticalPanel panel = new VerticalPanel();

        panel.add(new Label("Error"));
        panel.add(new Label(e.getMessage()));

        RootPanel.get(getPreLoadID()).setVisible(false);
        RootPanel.get(getLoadID()).add(panel);
    }

    public void setPageID(int pageID) {
        this.pageID = pageID;
    }

    protected void show(Widget panel) {
        show(pageID, panel);
    }
}
