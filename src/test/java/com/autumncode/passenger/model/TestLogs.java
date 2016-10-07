package com.autumncode.passenger.model;

import com.autumncode.logs.model.Log;
import com.autumncode.logs.model.TransactionLog;
import com.autumncode.logs.model.UserRequestLog;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;

public class TestLogs {
    private SessionFactory factory = null;

    @BeforeClass
    public void setup() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        factory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
    }

    void doInSession(Consumer<Session> command) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();

            command.accept(session);
            if (tx.isActive() &&
                    !tx.getRollbackOnly()) {
                tx.commit();
            } else {
                tx.rollback();
            }
        }

    }

    void log(String message) {
        doInSession((session) -> {
            Log log = new Log();
            log.setMessage(message);
            log.setCreatedAt(new Date());
            session.persist(log);
        });
    }

    @Test
    void testLogs() {
        log("message 1");
        logTransaction("trying to run select", "select foo from bar");
        log("message 2");
        logUserRequest("wanted help", "needed new password");
        log("sample log");
        doInSession((session) -> {
            Query<Log> query=session.createQuery("from Log l", Log.class);
            assertEquals(query.list().size(), 5);
        });
        doInSession((session) -> {
            Query<Log> query=session.createQuery("from Log l where l.message like :term", Log.class);
            query.setParameter("term", "message%");
            assertEquals(query.list().size(), 2);
        });
        doInSession((session) -> {
            Query<Log> query=session.createQuery("from Log l where type(l) = Log", Log.class);
            assertEquals(query.list().size(), 3);
        });

    }

    private void logUserRequest(String message, String request) {
        doInSession((session) -> {
            UserRequestLog log = new UserRequestLog();
            log.setMessage(message);
            log.setRequest(request);
            log.setCreatedAt(new Date());
            session.persist(log);
        });
    }

    private void logTransaction(String message, String statement) {
        doInSession((session) -> {
            TransactionLog log = new TransactionLog();
            log.setMessage(message);
            log.setStatement(statement);
            log.setCreatedAt(new Date());
            session.persist(log);
        });
    }
}
