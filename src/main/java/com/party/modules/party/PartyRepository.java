package com.party.modules.party;

import com.party.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface PartyRepository extends JpaRepository<Party, Long>, PartyRepositoryExtension {

    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags", "platforms", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Party findByPath(String path);

    @EntityGraph(attributePaths = {"tags", "managers"})
    Party findPartyWithTagsByPath(String path);

    @EntityGraph(attributePaths = {"platforms", "managers"})
    Party findPartyWithPlatformsByPath(String path);

    @EntityGraph(attributePaths = "managers")
    Party findPartyWithManagersByPath(String path);

    @EntityGraph(attributePaths = "members")
    Party findPartyWithMembersByPath(String path);

    Party findPartyOnlyByPath(String path);

    @EntityGraph(attributePaths = {"platforms", "tags"})
    Party findPartyWithTagsAndPlatformsById(Long id);

    @EntityGraph(attributePaths = {"members", "managers"})
    Party findPartyWithManagersAndMembersById(Long id);

    @EntityGraph(attributePaths = {"platforms", "tags"})
    List<Party> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Party> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Party> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}
