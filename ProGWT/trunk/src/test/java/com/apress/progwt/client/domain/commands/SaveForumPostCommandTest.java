package com.apress.progwt.client.domain.commands;

import junit.framework.TestCase;

import com.apress.progwt.client.domain.ForumPost;
import com.apress.progwt.client.domain.School;
import com.apress.progwt.client.domain.SchoolForumPost;
import com.apress.progwt.client.domain.User;
import com.apress.progwt.client.exception.SiteException;

public class SaveForumPostCommandTest extends TestCase {

    private static final String TITLE = "testtitle1";
    private static final String TEXT = "test text";

    private static final String TEXT_XSS = "dsfsdf<em>sdft</em>est<SCRIPT SRC=http://ha.ckers.org/xss.js></SCRIPT> text";
    private static final String TEXT_XSS_C = "dsfsdf<em>sdft</em>est text";

    public void testExecute() throws SiteException {

        School sc = new School();
        sc.setId(66);
        sc.setName("Dart");

        User au = new User();
        au.setId(77);
        au.setUsername("Author");

        ForumPost fp = new SchoolForumPost(sc, au, TITLE, TEXT, null);

        SaveForumPostCommand command = new SaveForumPostCommand(fp);

        MockCommandService commandService = new MockCommandService(
                command);

        assertNull(command.getToSave());

        command.execute(commandService);

        ForumPost saved = command.getToSave();
        assertEquals(TITLE, saved.getPostTitle());
        assertEquals(TEXT, saved.getPostString());

        assertEquals(sc, saved.getTopic());
        assertEquals(au, saved.getAuthor());
        assertEquals(null, saved.getThreadPost());

    }

    public void testExecuteNoXSS() throws SiteException {

        School sc = new School();
        sc.setId(66);
        sc.setName("Dart");

        User au = new User();
        au.setId(77);
        au.setUsername("Author");

        ForumPost fp = new SchoolForumPost(sc, au, TEXT_XSS, TEXT_XSS,
                null);

        SaveForumPostCommand command = new SaveForumPostCommand(fp);

        MockCommandService commandService = new MockCommandService(
                command);

        assertNull(command.getToSave());

        command.execute(commandService);

        ForumPost saved = command.getToSave();
        assertEquals(TEXT_XSS_C, saved.getPostTitle());
        assertEquals(TEXT_XSS_C, saved.getPostString());

        assertEquals(sc, saved.getTopic());
        assertEquals(au, saved.getAuthor());
        assertEquals(null, saved.getThreadPost());

    }
}
