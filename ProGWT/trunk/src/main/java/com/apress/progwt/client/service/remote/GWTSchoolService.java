package com.apress.progwt.client.service.remote;

import java.util.List;

import com.apress.progwt.client.domain.ProcessType;
import com.apress.progwt.client.domain.School;
import com.apress.progwt.client.domain.commands.AbstractCommand;
import com.apress.progwt.client.exception.BusinessException;
import com.apress.progwt.client.exception.SiteException;
import com.google.gwt.user.client.rpc.RemoteService;

public interface GWTSchoolService extends RemoteService {

    List<School> getSchoolsMatching(String match)
            throws BusinessException;

    Boolean executeAndSaveCommand(AbstractCommand comm)
            throws SiteException;

    List<ProcessType> matchProcessType(String queryString)
            throws SiteException;

    List<School> getAllSchools() throws SiteException;
}
