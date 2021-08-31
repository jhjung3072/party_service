package com.party.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean partyCreatedByEmail;

    private boolean partyCreatedByWeb;

    private boolean partyEnrollmentResultByEmail;

    private boolean partyEnrollmentResultByWeb;

    private  boolean partyUpdatedByEmail;

    private  boolean partyUpdatedByWeb;


    public void generateEmailCheckToken() {
        this.emailCheckToken= UUID.randomUUID().toString();
    }
}
