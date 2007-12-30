package com.apress.progwt.server.service.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.apress.progwt.client.domain.Loadable;
import com.apress.progwt.client.domain.ProcessType;
import com.apress.progwt.client.domain.School;
import com.apress.progwt.client.domain.User;
import com.apress.progwt.client.domain.commands.CommandService;
import com.apress.progwt.client.domain.commands.SiteCommand;
import com.apress.progwt.client.domain.dto.SchoolThreads;
import com.apress.progwt.client.exception.SiteException;
import com.apress.progwt.server.dao.SchoolDAO;
import com.apress.progwt.server.domain.SchoolPopularity;
import com.apress.progwt.server.service.SchoolService;
import com.apress.progwt.server.service.SearchService;
import com.apress.progwt.server.service.UserService;

@Transactional
public class SchoolServiceImpl implements SchoolService, CommandService {

    private static final Logger log = Logger
            .getLogger(SchoolServiceImpl.class);

    private SchoolDAO schoolDAO;

    private UserService userService;
    private SearchService searchService;

    public School getSchoolDetails(String schoolname) {

        return schoolDAO.getSchoolFromName(schoolname);
    }

    @Required
    public void setSchoolDAO(SchoolDAO schoolDAO) {
        this.schoolDAO = schoolDAO;
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public List<SchoolPopularity> getPopularSchools() {
        List<SchoolPopularity> ranked = new LinkedList<SchoolPopularity>();
        for (School school : getTopSchools()) {
            ranked.add(new SchoolPopularity(school,
                    Math.random() * 5 - 2.5));
        }
        return ranked;
    }

    public SiteCommand executeAndSaveCommand(SiteCommand command)
            throws SiteException {
        return executeAndSaveCommand(command, true);
    }

    /**
     * Can turn off the userCache which avoid some problems with our
     * transactional testing.
     */
    public SiteCommand executeAndSaveCommand(SiteCommand command,
            boolean useUserCache) throws SiteException {

        User u = userService.getCurrentUser(useUserCache);
        if (u != null) {
            log.info(command
                    + " "
                    + userService.getCurrentUser(useUserCache)
                            .getUsername());
        } else {
            log.warn(command + " attempted by anonymous ");
        }

        log.info("Going to execute Command...");

        command.execute(this);

        // schoolDAO.executeAndSaveCommand(u, command);

        log.info("Executed Command. Saving...");

        // hydrateCommand(command, useUserCache);
        // command.executeCommand();

        // saveCommand(command);
        //
        // log.info("Saved");
        //
        // deleteCommand(command);
        return command;
    }

    /**
     * TODO protect?
     */
    public void save(Loadable loadable) {
        if (loadable instanceof User) {
            User user = (User) loadable;
            userService.save(user);
        } else {
            schoolDAO.save(loadable);
        }
    }

    // private void hydrateCommand(AbstractCommand command,
    // boolean useUserCache) throws SiteException {
    //
    // log.debug("Hydrate: " + command);
    //
    // User current = userService.getCurrentUser(useUserCache);
    // command.setCurrentUser(current);
    //
    // List<Loadable> loadedObjs = new ArrayList<Loadable>(command
    // .getObjects().size());
    // for (Loadable loadable : command.getObjects()) {
    //
    // Loadable l = schoolDAO.get(loadable.getClass(), loadable
    // .getId());
    //
    // loadedObjs.add(l);
    // }
    // command.setObjects(loadedObjs);
    //
    // // int i = 0;
    // // for (Class<Loadable> loadable : command.getClasses()) {
    // //
    // // log.debug("loadable: " + loadable);
    // // log.debug("command.getLookups().get() "
    // // + command.getIds().get(i));
    // //
    // // Loadable l = schoolDAO.get(loadable, command.getIds().get(i));
    // //
    // // command.addObject(l);
    // // i++;
    // // }
    //
    // }

    public List<School> getTopSchools() {
        return schoolDAO.getAllSchools().subList(0, 10);
    }

    /**
     * Search for "match*" using searchService
     */
    public List<School> getSchoolsMatching(String match) {
        return searchService.searchForSchool(match);
        // return schoolDAO.getSchoolsMatching(match);
    }

    public List<ProcessType> matchProcessType(String queryString) {

        return schoolDAO.matchProcessType(queryString);
    }

    public List<School> getAllSchools() {
        return schoolDAO.getAllSchools();
    }

    public <T> T get(Class<T> clazz, long id) {
        return (T) schoolDAO.get((Class<? extends Loadable>) clazz, id);

    }

    public SchoolThreads getThreads(long schoolID, int start, int max) {
        return schoolDAO.getThreads(schoolID, start, max);
    }

    @Required
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

}
