package com.party.modules.study;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.party.modules.account.QAccount;
import com.party.modules.tag.QTag;
import com.party.modules.tag.Tag;
import com.party.modules.zone.QZone;
import com.party.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension {

    public StudyRepositoryExtensionImpl() {
        super(Study.class);
    }

    //키워드로 검색 및 페이징 처리
    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue() // 공개된 스터디
                .and(study.title.containsIgnoreCase(keyword)) //and 제목이 대소문자 상관없이 keyword
                .or(study.tags.any().title.containsIgnoreCase(keyword)) // or 태그의 이름이 대소문자 상관없이 keyword
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword))) //or 지역의 도시이름이 대소문자 상관없이 keyword
                // study를 가져오면서 tags,zones를 가져오기 위한 leftJoin -> N+1 쿼리 해결
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .distinct();
        // 쿼리에 Pagination 적용
        JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        // paging 정보를 포함하여 가져오기 위해 fetchResults 사용
        QueryResults<Study> fetchResults = pageableQuery.fetchResults();
        //content, pageable, total
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    //account가 갖고 있는 tags 와 zones 에 대한 study 목록 조회
    //페이징 처리 없이 9개까지 보여줌
    @Override
    public List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue() //공개된 스터디
                .and(study.closed.isFalse()) // and 아직 종료되지 않았고
                .and(study.tags.any().in(tags))  // and 스터디의 태그가 account의 태그
                .and(study.zones.any().in(zones))) // and 스터디의 지역이 account의 지역
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .orderBy(study.publishedDateTime.desc())
                .distinct()
                .limit(9);
        return query.fetch();
    }
}
