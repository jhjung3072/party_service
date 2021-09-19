package com.party.modules.event;

import com.party.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);

    // Enrollment 조회시 event, study 도 조회
    @EntityGraph("Enrollment.withEventAndStudy")
    List<Enrollment> findByAccountAndAcceptedOrderByEnrolledAtDesc(Account account, boolean accepted);
}
