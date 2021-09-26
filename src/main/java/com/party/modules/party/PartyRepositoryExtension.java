package com.party.modules.party;

import com.party.modules.tag.Tag;
import com.party.modules.platform.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface PartyRepositoryExtension {

    Page<Party> findByKeyword(String keyword, Pageable pageable);

    //account가 갖고 있는 tags 와 platforms 에 대한 party 리스트
    List<Party> findByAccount(Set<Tag> tags, Set<Platform> platforms);

}
