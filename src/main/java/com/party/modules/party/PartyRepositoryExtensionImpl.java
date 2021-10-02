package com.party.modules.party;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.party.modules.tag.QTag;
import com.party.modules.tag.Tag;
import com.party.modules.platform.QPlatform;
import com.party.modules.platform.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class PartyRepositoryExtensionImpl extends QuerydslRepositorySupport implements PartyRepositoryExtension {

    public PartyRepositoryExtensionImpl() {
        super(Party.class);
    }

    //키워드 검색 및 페이징 처리
    @Override
    public Page<Party> findByKeyword(String keyword, Pageable pageable) {
        QParty party = QParty.party;
        JPQLQuery<Party> query = from(party).where(party.published.isTrue() // 공개된 파티
                .and(party.title.containsIgnoreCase(keyword)) //and 제목이 대소문자 상관없이 keyword
                .or(party.tags.any().title.containsIgnoreCase(keyword)) // or 태그의 이름이 대소문자 상관없이 keyword
                .or(party.platforms.any().koreanNameOfPlatform.containsIgnoreCase(keyword))) //or 플랫폼의 도시이름이 대소문자 상관없이 keyword
                // party를 가져오면서 tags,platforms를 가져오기 위한 leftJoin -> N+1 쿼리 해결
                .leftJoin(party.tags, QTag.tag).fetchJoin()
                .leftJoin(party.platforms, QPlatform.platform).fetchJoin()
                .distinct();
        // 쿼리에 Pagination 적용
        JPQLQuery<Party> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        // paging 정보를 포함하여  쿼리 결과를 가져오기 위해 fetchResults 사용
        QueryResults<Party> fetchResults = pageableQuery.fetchResults();
        //content, pageable, total
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    //account가 갖고 있는 tags 와 platforms 에 대한 party 목록 조회
    //페이징 처리 없이 9개까지 보여줌
    @Override
    public List<Party> findByAccount(Set<Tag> tags, Set<Platform> platforms) {
        QParty party = QParty.party;
        JPQLQuery<Party> query = from(party).where(party.published.isTrue() //공개된 파티
                        .and(party.recruiting.isTrue()) // and 모집중
                        .and(party.closed.isFalse()) // and 아직 종료되지 않았고
                        .and(party.tags.any().in(tags).or(party.platforms.any().in(platforms))))  // and 파티의 태그 or 플랫폼이 있고
                        .leftJoin(party.tags, QTag.tag).fetchJoin()
                        .leftJoin(party.platforms, QPlatform.platform).fetchJoin()
                        .orderBy(party.publishedDateTime.desc())
                        .distinct()
                        .limit(9);
                return query.fetch();
    }
}
