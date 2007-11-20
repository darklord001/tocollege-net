package com.apress.progwt.server.dao.hibernate;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.apress.progwt.client.domain.Bar;
import com.apress.progwt.client.domain.Foo;
import com.apress.progwt.client.domain.Loadable;
import com.apress.progwt.client.domain.ProcessType;
import com.apress.progwt.client.domain.RatingType;
import com.apress.progwt.client.domain.School;
import com.apress.progwt.server.dao.SchoolDAO;

public class SchoolDAOHibernateImpl extends HibernateDaoSupport implements
        SchoolDAO {

    private static final int DEFAULT_AUTOCOMPLET_MAX = 7;
    private static final Logger log = Logger
            .getLogger(SchoolDAOHibernateImpl.class);

    public Loadable get(Class<? extends Loadable> loadable, Long id) {
        return (Loadable) getHibernateTemplate().get(loadable, id);
    }

    public List<School> getAllSchools() {
        DetachedCriteria crit = DetachedCriteria.forClass(School.class);
        List<School> list = getHibernateTemplate().findByCriteria(crit,
                0, 10);
        return list;
    }

    public List<ProcessType> getDefaultProcessTypes() {
        DetachedCriteria crit = DetachedCriteria.forClass(
                ProcessType.class).add(
                Expression.eq("useByDefault", true)).addOrder(
                Order.asc("id"));

        return getHibernateTemplate().findByCriteria(crit);
    }

    public List<RatingType> getDefaultRatingTypes() {
        DetachedCriteria crit = DetachedCriteria.forClass(
                RatingType.class)
                .add(Expression.eq("useByDefault", true)).addOrder(
                        Order.asc("id"));

        return getHibernateTemplate().findByCriteria(crit);
    }

    public ProcessType getProcessForName(String string) {
        DetachedCriteria crit = DetachedCriteria.forClass(
                ProcessType.class).add(Expression.eq("name", string));

        return (ProcessType) DataAccessUtils
                .uniqueResult(getHibernateTemplate().findByCriteria(crit));
    }

    public School getSchoolFromName(String name) {
        return (School) DataAccessUtils
                .uniqueResult(getHibernateTemplate().find(
                        "from School where name=?", name));
    }

    public List<School> getSchoolsMatching(String match) {

        DetachedCriteria crit = DetachedCriteria.forClass(School.class)
                .add(Expression.ilike("name", match, MatchMode.ANYWHERE))
                .addOrder(Order.asc("name"));

        List<School> list = getHibernateTemplate().findByCriteria(crit,
                0, DEFAULT_AUTOCOMPLET_MAX);

        return list;
    }

    public List<ProcessType> matchProcessType(String queryString) {
        DetachedCriteria crit = DetachedCriteria.forClass(
                ProcessType.class)
                .add(
                        Expression.ilike("name", queryString,
                                MatchMode.ANYWHERE)).addOrder(
                        Order.asc("name"));

        List<ProcessType> list = getHibernateTemplate().findByCriteria(
                crit, 0, DEFAULT_AUTOCOMPLET_MAX);

        return list;

    }

    public Loadable save(Loadable loadable) {
        getHibernateTemplate().saveOrUpdate(loadable);
        return loadable;
    }

    public Foo saveF() {
        return (Foo) getHibernateTemplate().execute(
                new HibernateCallback() {

                    public Object doInHibernate(Session session)
                            throws HibernateException, SQLException {

                        Foo f = new Foo("myfoo");

                        session.save(f);

                        Bar b = new Bar("A");

                        b.setFoo(f);
                        f.getBarList().add(b);

                        session.save(f);

                        return f;
                    }
                });

    }

}
