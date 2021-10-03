package com.party.modules.account;

import com.querydsl.core.types.Predicate;
import com.party.modules.tag.Tag;
import com.party.modules.platform.Platform;

import java.util.Set;


public class AccountPredicates {

    public static Predicate findByTagsOrPlatforms(Set<Tag> tags, Set<Platform> platforms) {
        QAccount account = QAccount.account;
        // platforms,tags를 갖고 있는 accounts 를 조회
        return account.platforms.any().in(platforms).or(account.tags.any().in(tags));
    }

}
