package com.party.modules.event;

import com.party.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.EntityGraph;

import javax.persistence.*;
import java.time.LocalDateTime;

@NamedEntityGraph(
        name = "Enrollment.withEventAndParty",
        attributeNodes = {
                @NamedAttributeNode(value = "event", subgraph = "party")  // enrollment.event.party 조회
        },
        subgraphs = @NamedSubgraph(name = "party", attributeNodes = @NamedAttributeNode("party")) //subgraph party 정의
)
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;

}
