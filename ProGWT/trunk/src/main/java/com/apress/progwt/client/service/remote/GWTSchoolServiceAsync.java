package com.apress.progwt.client.service.remote;

import java.util.List;

import com.apress.progwt.client.domain.ProcessType;
import com.apress.progwt.client.domain.School;
import com.apress.progwt.client.domain.commands.AbstractCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GWTSchoolServiceAsync {

    void getSchoolsMatching(String match,
            AsyncCallback<List<School>> callback);

    void executeAndSaveCommand(AbstractCommand comm,
            AsyncCallback<Boolean> callback);

    void matchProcessType(String queryString,
            AsyncCallback<List<ProcessType>> callback);

}
