package com.party.modules.event;

import com.party.modules.account.Account;
import com.party.modules.account.UserAccount;
import com.party.modules.study.Study;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// event 조회할때 enrollments도 조회
@NamedEntityGraph(
        name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Event {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study;

    @ManyToOne
    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column
    private Integer limitOfEnrollments;

    @OneToMany(mappedBy = "event")
    @OrderBy("enrolledAt")
    private List<Enrollment> enrollments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    //참가 신청 가능 여부
    public boolean isEnrollableFor(UserAccount userAccount) {
        //마감되지 않았고, 참석하지 않았고, 이미 신청하지 않은 경우
        return isNotClosed() && !isAttended(userAccount) && !isAlreadyEnrolled(userAccount);
    }

    //참가 취소 가능 여부
    public boolean isDisenrollableFor(UserAccount userAccount) {
        //마감되지 않았고, 참석하지 않았고, 이미 신청한 경우
        return isNotClosed() && !isAttended(userAccount) && isAlreadyEnrolled(userAccount);
    }

    //마감되었는지
    private boolean isNotClosed() {
        //현재 시간이 참가신청 종료시간보다 앞서는 경우
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    //출석여부
    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    // 모임에 몇자리 남았는지
    public int numberOfRemainSpots() {
        // 제한 모집인원 수 - 현재 승인된 모집 인원 수
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    // 이미 참가신청했는지 여부
    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    // 승인된 참가자 수
    public long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    //참가 신청 추가
    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setEvent(this);
    }

    //참가 신청 삭제
    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.setEvent(null);
    }

    //모집 대기열 승인 가능 여부
    public boolean isAbleToAcceptWaitingEnrollment() {
        //모임 타입이 선착순이고 모집할 제한인원이 승인된 인원보다 클 경우
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    //참가신청 수락가능 여부
    public boolean canAccept(Enrollment enrollment) {
        // 팀장이 확인하는 모임 타입이고, 모임중에 해당 모임이 있고, 모집할 인원이 승인된 인원보다 크고,
        // 모임에 출석하지 않았고, 모임에 승인되지 않은 경우
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()
                && !enrollment.isAttended()
                && !enrollment.isAccepted();
    }

    //참가신청 취소가능 여부
    public boolean canReject(Enrollment enrollment) {
        //팀장이 확인하는 모입 타입이고, 모임중에 해당 모임이 있고,
        //모임에 출석하지 않았고, 모임에 승인된 경우
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }

    //대기중인 참가신청 리스트
    private List<Enrollment> getWaitingList() {
        //모임에 승인되지 않은 신청 리스트
        return this.enrollments.stream().filter(enrollment -> !enrollment.isAccepted()).collect(Collectors.toList());
    }


    //모집인원이 늘었을 때 자동으로 승인
    public void acceptWaitingList() {
        if (this.isAbleToAcceptWaitingEnrollment()) {
            var waitingList = getWaitingList();
            //  승인할 수 있는 인원 수= 모집인원 수 - 현재 승인된 인원  과  현재 대기중인 인원중 작은 값
            int numberToAccept = (int) Math.min(this.limitOfEnrollments - this.getNumberOfAcceptedEnrollments(), waitingList.size());
            // 대기중인 인원에서 승인할 수 있는 인원수만큼 반복하면서 승인을 true
             waitingList.subList(0, numberToAccept).forEach(e -> e.setAccepted(true));
        }
    }

    // 기존의 승인된 인원이 모임을 취소 -> 첫번째로 대기중인 인원 승인
    public void acceptNextWaitingEnrollment() {
        if (this.isAbleToAcceptWaitingEnrollment()) {
            Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment();
            if (enrollmentToAccept != null) {
                enrollmentToAccept.setAccepted(true);
            }
        }
    }

    // 첫번째로 대기중인 인원
    private Enrollment getTheFirstWaitingEnrollment() {
        for (Enrollment e : this.enrollments) {
            if (!e.isAccepted()) {
                return e;
            }
        }

        return null;
    }

    // 참가 신청 승인
    public void accept(Enrollment enrollment) {
        // 팀장이 확인하는 모임 타입이고, 모집인원 수가 승인된 인원 수보다 클 경우
        if (this.eventType == EventType.CONFIRMATIVE
                && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()) {
            enrollment.setAccepted(true);
        }
    }

    // 참가 신청 거부
    public void reject(Enrollment enrollment) {
        // 팀장이 확인하는 모임 타입일 경우
        if (this.eventType == EventType.CONFIRMATIVE) {
            enrollment.setAccepted(false);
        }
    }
}
