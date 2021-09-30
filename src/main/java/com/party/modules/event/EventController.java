package com.party.modules.event;

import com.party.modules.account.CurrentAccount;
import com.party.modules.account.Account;
import com.party.modules.party.Party;
import com.party.modules.event.form.EventForm;
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
    private final EventRepository eventRepository;
    private final PartyRepository partyRepository;
    private final EnrollmentRepository enrollmentRepository;

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
        return "redirect:/party/" + party.getEncodedPath() + "/events/";
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
        List<Event>events=eventRepository.findByParty(party);
        model.addAttribute("events", events);
        model.addAttribute(account);
        model.addAttribute(party);

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




}
