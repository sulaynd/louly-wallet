package com.meridian.transfer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    private String displayName;

    /** The account holder's own phone number, e.g. "+1 514 555 0134" */
    private String phoneNumber;

    /** Country name the account holder is based in, e.g. "Canada" */
    private String country;

    private String flagEmoji;

    /** "USER" for regular accounts, "ADMIN" for customer-service accounts that can manage rates. */
    @Column(nullable = false)
    private String role = "USER";
}
