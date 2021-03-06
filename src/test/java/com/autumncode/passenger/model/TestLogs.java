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
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;

public class TestLogs {
    private SessionFactory factory = null;

    @BeforeSuite
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

    @BeforeTest
    public void populateData() {
        log("message 1");
        logTransaction("trying to run select", "select foo from bar");
        log("message 2");
        logUserRequest("wanted help", "needed new password");
        log("sample log");
        logTransaction("message query", "select foo from bar with parameter");
    }

    private void log(String message) {
        doInSession((session) -> {
            Log log = new Log();
            log.setMessage(message);
            log.setCreatedAt(new Date());
            session.persist(log);
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

    @Test
    void testAllLogs() {
        doInSession((session) -> {
            Query<Log> query = session.createQuery("from Log l", Log.class);
            assertEquals(query.list().size(), 6);
        });
    }

    @Test
    public void testSelectOfAllLogs() {
        doInSession((session) -> {
            Query<Log> query = session.createQuery("from Log l where l.message like :message", Log.class);
            query.setParameter("message", "message%");
            assertEquals(query.list().size(), 3);
        });
    }

    @Test
    public void testSelectOfOnlyLogTypes() {
        doInSession((session) -> {
            Query<Log> query = session.createQuery("from Log l where type(l) = Log", Log.class);
            assertEquals(query.list().size(), 3);
        });
    }

    @Test
    public void testSelectOfOnlyLogTypesWithLike() {
        doInSession((session) -> {
            Query<Log> query = session.createQuery("from Log l where type(l) = Log and l.message like :message", Log.class);
            query.setParameter("message", "message%");
            assertEquals(query.list().size(), 2);
        });
    }
    @Test
    public void testSelectOfOnlyTransactionLogTypesWithLike() {
        doInSession((session) -> {
            Query<Log> query = session.createQuery("from Log l where type(l) = TransactionLog and l.message like :message", Log.class);
            query.setParameter("message", "message%");
            assertEquals(query.list().size(), 1);
        });
    }
}
