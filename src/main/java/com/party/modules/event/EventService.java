package com.party.modules.event;

import com.party.modules.account.Account;
import com.party.modules.event.event.EnrollmentAcceptedEvent;
import com.party.modules.event.event.EnrollmentRejectedEvent;
import com.party.modules.party.Party;
import com.party.modules.event.form.EventForm;
import com.party.modules.party.event.PartyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 모임 생성
    public Event createEvent(Event event, Party party, Account account) {
        event.setCreatedBy(account);
        event.setParty(party);
        eventPublisher.publishEvent(new PartyUpdateEvent(event.getParty(),
                "'" + event.getTitle() + "' 모임을 만들었습니다."));
        return eventRepository.save(event);
    }

    // 모임 수정
    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        eventPublisher.publishEvent(new PartyUpdateEvent(event.getParty(),
                "'" + event.getTitle() + "' 모임 정보를 수정했으니 확인하세요."));
    }

    // 모임 취소
    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        eventPublisher.publishEvent(new PartyUpdateEvent(event.getParty(),
                "'" + event.getTitle() + "' 모임을 취소했습니다."));
    }

}
