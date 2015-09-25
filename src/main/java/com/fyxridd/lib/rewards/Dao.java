package com.fyxridd.lib.rewards;

import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.rewards.api.model.RewardsUser;
import org.bukkit.event.Listener;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Collection;
import java.util.List;

public class Dao implements Listener {
    private SessionFactory sessionFactory;

	public Dao() {
        sessionFactory = CorePlugin.getSessionFactory();
	}

    public void saveOrUpdates(Collection c) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            for (Object o:c) session.saveOrUpdate(o);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    public void deletes(Collection c) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            for (Object o:c) session.delete(o);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    public List<RewardsUser> getRewardsUsers(String name) {
        Session session = sessionFactory.openSession();
        List<RewardsUser> result;
        try {
            session.beginTransaction();
            result = session.createQuery("from RewardsUser where name=:name").setParameter("name", name).list();
            session.getTransaction().commit();
            return result;
        } finally {
            session.close();
        }
    }
}
