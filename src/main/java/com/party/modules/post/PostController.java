package com.party.modules.post;

import com.party.modules.account.CurrentAccount;
import com.party.modules.account.Account;
import com.party.modules.party.Party;
import com.party.modules.post.form.PostForm;
import com.party.modules.party.PartyRepository;
import com.party.modules.party.PartyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/party/{id}")
@RequiredArgsConstructor
public class PostController {

    private final PartyService partyService;
    private final PostService postService;
    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    private final PartyRepository partyRepository;

    // 새 모임 생성 Get
    @GetMapping("/new-post")
    public String newPostForm(@CurrentAccount Account account, @PathVariable Long id, Model model) {
        Party party = partyService.getPartyToUpdateStatus(account, id);
        model.addAttribute(party);
        model.addAttribute(account);
        model.addAttribute(new PostForm());
        return "post/form";
    }

    // 새 모임 생성 Post
    @PostMapping("/new-post")
    public String newPostSubmit(@CurrentAccount Account account, @PathVariable Long id,
                                 @Valid PostForm postForm, Errors errors, Model model) {
        Party party = partyService.getPartyToUpdateStatus(account, id);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(party);
            return "post/form";
        }

        Post post = postService.createPost(modelMapper.map(postForm, Post.class), party, account);
        return "redirect:/party/" + party.getId() + "/posts/";
    }


    // 모임 전체 목록 뷰
    @GetMapping("/posts")
    public String viewPartyPosts(@CurrentAccount Account account, @PathVariable Long id, Model model) {
        Party party = partyService.getParty(id);
        List<Post> posts = postRepository.findByParty(party);
        model.addAttribute("posts", posts);
        model.addAttribute(account);
        model.addAttribute(party);

        return "party/posts";
    }

    // 모임 수정 뷰
    @GetMapping("/posts/{id}/edit")
    public String updatePostForm(@CurrentAccount Account account,
                                  @PathVariable Long id, @PathVariable("id") Post post, Model model) {
        Party party = partyService.getPartyToUpdate(account, id);
        model.addAttribute(party);
        model.addAttribute(account);
        model.addAttribute(post);
        model.addAttribute(modelMapper.map(post, PostForm.class));
        return "post/update-form";
    }

    // 모임 수정 Post
    @PostMapping("/posts/{id}/edit")
    public String updatePostSubmit(@CurrentAccount Account account, @PathVariable Long id,
                                    @PathVariable("id") Post post, @Valid PostForm postForm, Errors errors,
                                    Model model) {
        Party party = partyService.getPartyToUpdate(account, id);


        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(party);
            model.addAttribute(post);
            return "post/update-form";
        }

        postService.updatePost(post, postForm);
        return "redirect:/party/" + party.getId() +  "/posts/";
    }

    // 모임취소 Delete
    @DeleteMapping("/posts/{id}")
    public String cancelPost(@CurrentAccount Account account, @PathVariable Long id, @PathVariable("id") Post post) {
        Party party = partyService.getPartyToUpdateStatus(account, id);
        postService.deletePost(post);
        return "redirect:/party/" + party.getId() + "/posts";
    }




}
