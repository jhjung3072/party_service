package com.party.modules.event;

import com.party.modules.account.Account;
import com.party.modules.account.UserAccount;
import com.party.modules.party.Party;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Event {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Party party;

    @ManyToOne
    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

}
