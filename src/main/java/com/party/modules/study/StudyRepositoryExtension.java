package com.party.modules.study;

import com.party.modules.tag.Tag;
import com.party.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {

    Page<Study> findByKeyword(String keyword, Pageable pageable);

    //account가 갖고 있는 tags 와 zones 에 대한 study 리스트
    List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones);

}
