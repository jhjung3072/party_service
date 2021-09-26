package com.party.modules.event;

import com.party.modules.account.CurrentAccount;
import com.party.modules.account.Account;
import com.party.modules.party.Party;
import com.party.modules.event.form.EventForm;
import com.party.modules.event.validator.EventValidator;
import com.party.modules.party.PartyRepository;
import com.party.modules.party.PartyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/party/{path}")
@RequiredArgsConstructor
public class EventController {

    private final PartyService partyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;
    private final PartyRepository partyRepository;
    private final EnrollmentRepository enrollmentRepository;

    //eventForm 을 받을때 검증
    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }

    // 새 모임 생성 Get
    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Party party = partyService.getPartyToUpdateStatus(account, path);
        model.addAttribute(party);
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    // 새 모임 생성 Post
    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentAccount Account account, @PathVariable String path,
                                 @Valid EventForm eventForm, Errors errors, Model model) {
        Party party = partyService.getPartyToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(party);
            return "event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), party, account);
        return "redirect:/party/" + party.getEncodedPath() + "/events/" + event.getId();
    }

    // 모임 상세 페이지 뷰
    @GetMapping("/events/{id}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event,
                           Model model) {
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(partyRepository.findPartyWithManagersByPath(path)); //party.isManager
        return "event/view";
    }

    // 모임 전체 목록 뷰
    @GetMapping("/events")
    public String viewPartyEvents(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Party party = partyService.getParty(path);
        model.addAttribute(account);
        model.addAttribute(party);

        List<Event> events = eventRepository.findByPartyOrderByStartDateTime(party);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        //현재 시간보다 전에 종료된 모임이라면 oldEvents 그렇지 않으면 newEvents
        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);

        return "party/events";
    }

    // 모임 수정 뷰
    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentAccount Account account,
                                  @PathVariable String path, @PathVariable("id") Event event, Model model) {
        Party party = partyService.getPartyToUpdate(account, path);
        model.addAttribute(party);
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    // 모임 수정 Post
    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable("id") Event event, @Valid EventForm eventForm, Errors errors,
                                    Model model) {
        Party party = partyService.getPartyToUpdate(account, path);
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(party);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event, eventForm);
        return "redirect:/party/" + party.getEncodedPath() +  "/events/" + event.getId();
    }

    // 모임취소 Delete
    @DeleteMapping("/events/{id}")
    public String cancelEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Party party = partyService.getPartyToUpdateStatus(account, path);
        eventService.deleteEvent(event);
        return "redirect:/party/" + party.getEncodedPath() + "/events";
    }


    // 참가 신청 Post
    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentAccount Account account,
                                @PathVariable String path, @PathVariable("id") Event event) {
        Party party = partyService.getPartyToEnroll(path);
        eventService.newEnrollment(event, account);
        return "redirect:/party/" + party.getEncodedPath() + "/events/" + event.getId();
    }

    // 참가 신청 취소 Post
    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account,
                                   @PathVariable String path, @PathVariable("id") Event event) {
        Party party = partyService.getPartyToEnroll(path);
        eventService.cancelEnrollment(event, account);
        return "redirect:/party/" + party.getEncodedPath() + "/events/" + event.getId();
    }

    // 참가 신청 수락 Post
    @GetMapping("events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Party party = partyService.getPartyToUpdate(account, path);
        eventService.acceptEnrollment(event, enrollment);
        return "redirect:/party/" + party.getEncodedPath() + "/events/" + event.getId();
    }

    // 참가 신청 거부
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Party party = partyService.getPartyToUpdate(account, path);
        eventService.rejectEnrollment(event, enrollment);
        return "redirect:/party/" + party.getEncodedPath() + "/events/" + event.getId();
    }

    // 모임 출석 Post
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Party party = partyService.getPartyToUpdate(account, path);
        eventService.checkInEnrollment(enrollment);
        return "redirect:/party/" + party.getEncodedPath() + "/events/" + event.getId();
    }

    // 모임 출석 취소 Post
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                          @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Party party = partyService.getPartyToUpdate(account, path);
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/party/" + party.getEncodedPath() + "/events/" + event.getId();
    }

}
