package com.PAP_team_21.flashcards.entities.login;

import com.PAP_team_21.flashcards.entities.JsonViewConfig;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "Logins")
@Getter
@Setter
public class Login {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(JsonViewConfig.Public.class)
    private int id;

    @Column(name = "customer_id")
    @JsonView(JsonViewConfig.Public.class)
    private int customerId;

    @Column(name = "login_date")
    @JsonView(JsonViewConfig.Public.class)
    private LocalDate loginDate;

    @ManyToOne
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    public Login() {}

    public Login(int customerId) {
        this.customerId = customerId;
        this.loginDate = LocalDate.now();
    }

    public Login(int customerId, LocalDate loginDate) {
        this.customerId = customerId;
        this.loginDate = loginDate;
    }
}
