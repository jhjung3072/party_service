package com.party.modules.post;

import com.party.modules.party.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByParty(Party party);

}
