package com.PAP_team_21.flashcards.entities.customer;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerDaoImpl implements CustomerDao {

    private final EntityManager entityManager;

    @Autowired
    public CustomerDaoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(Customer customer) {
        entityManager.persist(customer);
    }

    @Override
    public List<Customer> findUserByUsername(String username) {
        String jpql = "SELECT u FROM Customer u WHERE u.username = :username";
        return entityManager.createQuery(jpql, Customer.class)
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    @Transactional
    public void update(Customer customer) {
        entityManager.merge(customer);
    }

    @Override
    public boolean checkIfEmailAvailable(String email) {
        String jpql = "SELECT u FROM Customer u";
        List<Customer> customers = entityManager.createQuery(jpql, Customer.class).getResultList();
        for (Customer customer : customers) {
            if (customer.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }
}


