package com.party.modules.post;

import com.party.modules.account.Account;
import com.party.modules.party.Party;
import com.party.modules.post.form.PostForm;
import com.party.modules.party.event.PartyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    // 추천 작품 게시글 생성
    public Post createPost(Post post, Party party, Account account) {
        post.setCreatedBy(account);
        post.setParty(party);
        eventPublisher.publishEvent(new PartyUpdateEvent(post.getParty(), " 새로운 추천 작품이 있습니다. '"+
                 post.getTitle() ));
        return postRepository.save(post);
    }

    // 추천 작품 게시글 수정
    public void updatePost(Post post, PostForm postForm) {
        modelMapper.map(postForm, post);
        eventPublisher.publishEvent(new PartyUpdateEvent(post.getParty(), " 추천 작품 게시글이 수정되었습니다. '"+
                post.getTitle() + "'"));
    }

    //  추천 작품 게시글 삭제
    public void deletePost(Post post) {
        postRepository.delete(post);
 
    }

}
