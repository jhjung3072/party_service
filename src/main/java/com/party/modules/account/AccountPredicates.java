package com.party.modules.account;

import com.querydsl.core.types.Predicate;
import com.party.modules.tag.Tag;
import com.party.modules.zone.Zone;

import java.util.Set;


public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QAccount account = QAccount.account;
        // zones,tags를 갖고 있는 accounts 를 조회
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }

}
