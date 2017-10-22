package ru.nsu.fit.endpoint.service.database;

import jersey.repackaged.com.google.common.collect.Lists;
import org.slf4j.Logger;
import ru.nsu.fit.endpoint.service.database.data.Customer;
import ru.nsu.fit.endpoint.service.database.data.Plan;
import ru.nsu.fit.endpoint.service.database.data.Subscription;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class DBService {
    // Constants
    private static final String INSERT_CUSTOMER = "INSERT INTO CUSTOMER(id, first_name, last_name, login, pass, balance) values ('%s', '%s', '%s', '%s', '%s', %s)";
    private static final String INSERT_SUBSCRIPTION = "INSERT INTO SUBSCRIPTION(id, customer_id, plan_id) values ('%s', '%s', '%s')";
    private static final String INSERT_PLAN = "INSERT INTO PLAN(id, name, details, fee) values ('%s', '%s', '%s', %s)";

    private static final String SELECT_CUSTOMER = "SELECT id FROM CUSTOMER WHERE login='%s'";
    private static final String SELECT_CUSTOMER_BY_ID = "SELECT * FROM CUSTOMER WHERE id='%s'";
    private static final String SELECT_CUSTOMERS = "SELECT * FROM CUSTOMER";
    private static final String UPDATE_CUSTOMER = "UPDATE CUSTOMER SET firstName='%s', lastName='%s', login='%s', pass='%s', balance=%s" +
            "  where id='%s'";
    public static final String COULD_NOT_UPDATE_CUSTOMER = "Could not update customer with id=%s";

    private static final String SELECT_PLAN_BY_NAME = "SELECT * FROM PLAN WHERE name='%s'";
    private static final String SELECT_PLANS = "SELECT * FROM PLAN";
    private static final String UPDATE_PLAN = "UPDATE PLAN set name='%s', details='%s', fee='%s' where id='%s')";
    private static final String REMOVE_PLAN = "REMOVE FROM PLAN where id='%s')";

    private static final String SELECT_SUBSCRIPTIONS_BY_CUSTOMER_ID = "SELECT * FROM SUBSCRIPTION WHERE customerId='%s')";
    private static final String REMOVE_SUBSCRIPTION = "REMOVE FROM SUBSCRIPTION where id='%s')";

    private Logger logger;
    private static final Object generalMutex = new Object();
    private Connection connection;

    public DBService(Logger logger) {
        this.logger = logger;
        init();
    }

    public Customer createCustomer(Customer customerData) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'createCustomer' was called with data: '%s'", customerData));

            customerData.setId(UUID.randomUUID());
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(
                        String.format(
                                INSERT_CUSTOMER,
                                customerData.getId(),
                                customerData.getFirstName(),
                                customerData.getLastName(),
                                customerData.getLogin(),
                                customerData.getPass(),
                                customerData.getBalance()));
                return customerData;
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public List<Customer> getCustomers() {
        synchronized (generalMutex) {
            logger.info("Method 'getCustomers' was called.");

            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(SELECT_CUSTOMERS);
                List<Customer> result = Lists.newArrayList();
                while (rs.next()) {
                    Customer customerData = Customer.builder()
                            .firstName(rs.getString(2))
                            .lastName(rs.getString(3))
                            .login(rs.getString(4))
                            .pass(rs.getString(5))
                            .balance(rs.getInt(6))
                            .build();

                    result.add(customerData);
                }
                return result;
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public UUID getCustomerIdByLogin(String customerLogin) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'getCustomerIdByLogin' was called with data '%s'.", customerLogin));

            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        String.format(
                                SELECT_CUSTOMER,
                                customerLogin));
                if (rs.next()) {
                    return UUID.fromString(rs.getString(1));
                } else {
                    logger.warn("Customer with login '" + customerLogin + " was not found");
                    return null;
                }
            } catch (SQLException ex) {
                logger.debug(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public Customer getCustomerById(UUID id) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'getCustomerById' was called with data '%s'.", id));

            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        String.format(
                                SELECT_CUSTOMER_BY_ID,
                                id));
                if (rs.next()) {
                    return Customer.builder()
                            .firstName(rs.getString(2))
                            .lastName(rs.getString(3))
                            .login(rs.getString(4))
                            .pass(rs.getString(5))
                            .balance(rs.getInt(6))
                            .build();
                } else {
                    logger.warn("Customer with login '" + id + " was not found");
                    return null;
                }
            } catch (SQLException ex) {
                logger.debug(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public Customer updateCustomer(Customer customer) {
        synchronized (generalMutex) {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(
                        String.format(
                                UPDATE_CUSTOMER,
                                customer.getLastName(),
                                customer.getFirstName(),
                                customer.getLogin(),
                                customer.getPass()));

                return customer;
            } catch (SQLException ex) {
                logger.debug(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public Plan createPlan(Plan plan) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'createPlan' was called with data '%s'.", plan));

            plan.setId(UUID.randomUUID());
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(
                        String.format(
                                INSERT_PLAN,
                                plan.getId(),
                                plan.getName(),
                                plan.getDetails(),
                                plan.getFee()));
                return plan;
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public UUID getPlanIdByName(String planName) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'getPlanIdByName' was called with data '%s'.", planName));

            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        String.format(
                                SELECT_PLAN_BY_NAME,
                                planName));
                if (rs.next()) {
                    return UUID.fromString(rs.getString(1));
                } else {
                    logger.warn("Plan with name=" + planName + " was not found");
                    return null;
                }
            } catch (SQLException ex) {
                logger.debug(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public Plan getPlanById(UUID id) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'getCustomerById' was called with data '%s'.", id));

            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        String.format(
                                SELECT_CUSTOMER_BY_ID,
                                id));
                if (rs.next()) {
                    return Plan.builder()
                            .name(rs.getString(2))
                            .details(rs.getString(3))
                            .fee(rs.getInt(4))
                            .build();
                } else {
                    logger.warn("Customer with login '" + id + " was not found");
                    return null;
                }
            } catch (SQLException ex) {
                logger.debug(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public Plan updatePlan(Plan plan) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'updatePlan' was called with data '%s'.", plan));

            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(
                        String.format(
                                UPDATE_PLAN,
                                plan.getName(),
                                plan.getDetails(),
                                plan.getFee(),
                                plan.getId()));
                return plan;
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public void removePlan(UUID id) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'removePlan' was called with data '%s'.", id));

            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(
                        String.format(
                                REMOVE_PLAN,
                                id));
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public List<Plan> getPlans() {
        synchronized (generalMutex) {
            logger.info("Method 'getPlans' was called.");

            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(SELECT_PLANS);
                List<Plan> result = Lists.newArrayList();
                while (rs.next()) {
                    Plan plan = Plan.builder()
                            .id(UUID.fromString(rs.getString(1)))
                            .name(rs.getString(2))
                            .details(rs.getString(3))
                            .fee(rs.getInt(4))
                            .build();

                    result.add(plan);
                }
                return result;
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public List<Subscription> getSubscriptionsByCustomerId(UUID customerId) {
        synchronized (generalMutex) {
            logger.info("Method 'getSubscriptionsByCustomerId' was called.");

            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery( String.format(SELECT_SUBSCRIPTIONS_BY_CUSTOMER_ID, customerId));
                List<Subscription> result = Lists.newArrayList();
                while (rs.next()) {
                    Subscription subscription = Subscription.builder()
                            .id(UUID.fromString(rs.getString(1)))
                            .customerId(UUID.fromString(rs.getString(2)))
                            .planId(UUID.fromString(rs.getString(3)))
                            .build();

                    result.add(subscription);
                }
                return result;
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public Subscription createSubscription(Subscription subscription) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'createSubscription' was called with data '%s'.", subscription));

            subscription.setId(UUID.randomUUID());
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(
                        String.format(
                                INSERT_SUBSCRIPTION,
                                subscription.getId(),
                                subscription.getCustomerId(),
                                subscription.getPlanId()));
                return subscription;
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public void removeSubscription(UUID id) {
        synchronized (generalMutex) {
            logger.info(String.format("Method 'removeSubscription' was called with data '%s'.", id));

            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(
                        String.format(
                                REMOVE_SUBSCRIPTION,
                                id));
            } catch (SQLException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }

    private void init() {
        logger.debug("-------- MySQL JDBC Connection Testing ------------");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            logger.debug("Where is your MySQL JDBC Driver?", ex);
            throw new RuntimeException(ex);
        }

        logger.debug("MySQL JDBC Driver Registered!");

        try {
            connection = DriverManager
                    .getConnection(
                            "jdbc:mysql://localhost:3306/testmethods?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false",
                            "user",
                            "user");
        } catch (SQLException ex) {
            logger.error("Connection Failed! Check output console", ex);
            throw new RuntimeException(ex);
        }

        if (connection != null) {
            logger.debug("You made it, take control your database now!");
        } else {
            logger.error("Failed to make connection!");
        }
    }
}
