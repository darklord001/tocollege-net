package com.apress.progwt.server.service.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.apress.progwt.client.domain.ForumPost;
import com.apress.progwt.client.domain.Loadable;
import com.apress.progwt.client.domain.ProcessType;
import com.apress.progwt.client.domain.School;
import com.apress.progwt.client.domain.User;
import com.apress.progwt.client.domain.commands.CommandService;
import com.apress.progwt.client.domain.commands.SiteCommand;
import com.apress.progwt.client.domain.dto.PostsList;
import com.apress.progwt.client.domain.forum.ForumTopic;
import com.apress.progwt.client.domain.forum.RecentForumPostTopic;
import com.apress.progwt.client.exception.BusinessException;
import com.apress.progwt.client.exception.SiteException;
import com.apress.progwt.client.exception.SiteSecurityException;
import com.apress.progwt.server.dao.SchoolDAO;
import com.apress.progwt.server.domain.SchoolPopularity;
import com.apress.progwt.server.service.SchoolService;
import com.apress.progwt.server.service.SearchService;
import com.apress.progwt.server.service.UserService;
import com.apress.progwt.server.util.HTMLInputFilter;

@Transactional
public class SchoolServiceImpl implements SchoolService, CommandService {

    private static final HTMLInputFilter htmlFilter = new HTMLInputFilter();

    private static final Logger log = Logger
            .getLogger(SchoolServiceImpl.class);

    private SchoolDAO schoolDAO;
    private SearchService searchService;
    private UserService userService;

    public void assertUserIsAuthenticated(User toCheck)
            throws SecurityException {
        User loggedIn = userService.getCurrentUser();
        if (loggedIn == null || !loggedIn.equals(toCheck)) {
            throw new SecurityException("Logged in: " + loggedIn
                    + " Requested: " + toCheck);
        }
    }

    public void delete(Loadable loadable) {
        schoolDAO.delete(loadable);
    }

    public String escapeHtml(String string) {
        return StringEscapeUtils.escapeHtml(string);
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

        User loggedIn = userService.getCurrentUser(useUserCache);

        if (loggedIn == null) {
            log.warn(command + " attempted by anonymous ");
        }

        if (!command.haveYouSecuredYourselfAndFilteredUserInput()) {
            throw new BusinessException("Command " + command
                    + " hasn't secured.");
        }
        if (!userService.getToken(loggedIn).equals(command.getToken())) {
            log.warn("Possible XSRF: |" + command.getToken()
                    + "| expected |" + userService.getToken(loggedIn)
                    + "|");
            throw new SiteSecurityException("Invalid Session "
                    + command.getToken());
        } else {
            log.info("Tokens equal: " + userService.getToken(loggedIn));
        }
        if (loggedIn != null) {
            log.info(command + " " + loggedIn.getUsername());
        }

        log.info("Going to execute Command...");

        command.execute(this);

        log.info("Executed Command. Saving...");

        return command;
    }

    public String filterHTML(String input) {
        return htmlFilter.filter(input);
    }

    public <T> T get(Class<T> clazz, long id) {
        return (T) schoolDAO.get((Class<? extends Loadable>) clazz, id);

    }

    public List<School> getAllSchools() {
        return schoolDAO.getAllSchools(0, 2500);
    }

    public PostsList getForum(ForumTopic forumTopic, int start, int max) {
        if (forumTopic instanceof User) {
            User user = (User) forumTopic;
            return schoolDAO.getUserThreads(user.getId(), start, max);
        } else if (forumTopic instanceof School) {
            School school = (School) forumTopic;
            return schoolDAO.getSchoolThreads(school.getId(), start, max);
        } else if (forumTopic instanceof ForumPost) {
            ForumPost post = (ForumPost) forumTopic;
            return schoolDAO.getPostsForThread(post, start, max);
        } else if (forumTopic instanceof RecentForumPostTopic) {
            return schoolDAO.getRecentForumPosts(start, max);
        } else {
            throw new RuntimeException("Unknown forumTopic: "
                    + forumTopic);
        }
    }

    public List<SchoolPopularity> getPopularSchools() {
        List<SchoolPopularity> ranked = new LinkedList<SchoolPopularity>();
        for (School school : getTopSchools(0, 10)) {
            ranked.add(new SchoolPopularity(school, school
                    .getPopularityCounter()));
        }
        return ranked;
    }

    public School getSchoolDetails(String schoolname) {

        School school = schoolDAO.getSchoolFromName(schoolname);

        incrementSchoolPopularity(school);

        return school;
    }

    private void incrementSchoolPopularity(final School school) {
        Thread addTagThread = new Thread() {
            public void run() {
                try {
                    schoolDAO.incrementSchoolPopularity(school);
                } catch (Exception e) {
                    log.error("IncrementSchoolPopularity " + e);
                }

                log.info("Increment Complete");
            }
        };
        addTagThread.setPriority(Thread.MIN_PRIORITY);
        addTagThread.start();
    }

    public List<School> getSchoolsStarting(String match, int start,
            int max) {
        return schoolDAO.getSchoolsMatching(match, start, max);
    }

    /**
     * Search for "match*" using searchService
     * 
     * @throws SiteException
     */
    public List<String> getSchoolStringsMatching(String match)
            throws SiteException {
        return searchService.searchForSchool(match);
        // return schoolDAO.getSchoolsMatching(match);
    }

    public PostsList getSchoolThreads(long schoolID, int start, int max) {
        return schoolDAO.getSchoolThreads(schoolID, start, max);
    }

    public List<School> getTopSchools(int start, int max) {
        return schoolDAO.getAllSchools(start, max);
    }

    public List<User> getUsersInterestedIn(School school) {
        return schoolDAO.getUsersInterestedIn(school);
    }

    public List<ProcessType> matchProcessType(String queryString) {

        return schoolDAO.matchProcessType(queryString);
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

    @Required
    public void setSchoolDAO(SchoolDAO schoolDAO) {
        this.schoolDAO = schoolDAO;
    }

    @Required
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
