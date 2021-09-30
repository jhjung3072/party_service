package com.party.modules.party;

import com.party.modules.account.Account;
import com.party.modules.account.UserAccount;
import com.party.modules.event.EventType;
import com.party.modules.platform.Platform;
import com.party.modules.tag.Tag;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Party {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String kakaoLink;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Platform> platforms= new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column
    private Integer limitOfEnrollments;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    private int memberCount;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        //파티가 공개되었고, 모집중이고, 해당 유저가 멤버나 매니저가 아닐 경우 true
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);

    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public void addMemeber(Account account) {
        this.members.add(account);
    }

    public String getImage() {
        return image != null ? image : "/images/default_banner.png";
    }

    public void publish() {
        /* 파티가 종료되지 않았고 공개되지 않았으면,
           공개로 바꾸고, 현재 시간 입력*/
        if (!this.closed && !this.published) {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("파티를 공개할 수 없는 상태입니다. 파티를 이미 공개했거나 종료했습니다.");
        }
    }

    public void close() {
        /* 파티가 공개되었고, 종료되지 않았으면
           종료로 바꾸고, 현재 시간 입력*/
        if (this.published && !this.closed) {
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("파티를 종료할 수 없습니다. 파티를 공개하지 않았거나 이미 종료한 파티입니다.");
        }
    }

    //인원 모집 시작
    public void startRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 파티를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    //인원 모집 종료
    public void stopRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 파티를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    // 모집 수정 가능 여부
    public boolean canUpdateRecruiting() {
        // 파티가 공개되었고, 모집 수정 시간이 null 이거나 모집 수정 시간이 1시간 전 일 경우 true
        return this.published && this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    //파티 삭제 가능 여부
    public boolean isRemovable() {
        return !this.published; // TODO 모임을 했던 파티는 삭제할 수 없다.
    }

    // 멤버 추가
    public void addMember(Account account) {
        this.getMembers().add(account);
        this.memberCount++;
    }

    //멤버 삭제
    public void removeMember(Account account) {
        this.getMembers().remove(account);
        this.memberCount--;
    }

    //party 경로가 한글이 쓰일 수 있으므로 URLEncoder 사용
    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    //해당 파티의 Manager 인지 구분
    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }
}
