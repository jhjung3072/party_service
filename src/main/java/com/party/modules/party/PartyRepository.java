package com.party.modules.party;

import com.party.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PartyRepository extends JpaRepository<Party, Long>, PartyRepositoryExtension {

    boolean existsById(Long id);

    @EntityGraph(attributePaths = {"tags", "platforms", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Party findPartyById(Long id);

    @EntityGraph(attributePaths = {"tags", "managers"})
    Party findPartyWithTagsById(Long id);

    @EntityGraph(attributePaths = {"platforms", "managers"})
    Party findPartyWithPlatformsById(Long id);

    @EntityGraph(attributePaths = "managers")
    Party findPartyWithManagersById(Long id);

    @EntityGraph(attributePaths = "members")
    Party findPartyWithMembersById(Long id);

    Party findPartyOnlyById(Long id);

    @EntityGraph(attributePaths = {"platforms", "tags"})
    Party findPartyWithTagsAndPlatformsById(Long id);

    @EntityGraph(attributePaths = {"members", "managers"})
    Party findPartyWithManagersAndMembersById(Long id);

    @EntityGraph(attributePaths = {"platforms", "tags"})
    List<Party> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Party> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Party> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}
