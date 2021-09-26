package com.party.modules.party.event;

import com.party.modules.party.Party;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PartyUpdateEvent {

    private final Party party;

    private final String message;

}
