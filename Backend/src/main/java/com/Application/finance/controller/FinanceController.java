package com.skyline.finance.controller;

import com.skyline.auth.model.User;
import com.skyline.auth.repository.UserRepository;
import com.skyline.finance.model.*;
import com.skyline.finance.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final ReimbursementRepository reimbursementRepository;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('TREASURER', 'CLUB_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getFinancialSummary() {
        List<JournalEntry> entries = journalEntryRepository.findAll();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (JournalEntry entry : entries) {
            for (JournalLine line : entry.getLines()) {
                if (line.getAccount().getType() == Account.AccountType.REVENUE) {
                    totalIncome = totalIncome.add(line.getCredit());
                } else if (line.getAccount().getType() == Account.AccountType.EXPENSE) {
                    totalExpenses = totalExpenses.add(line.getDebit());
                }
            }
        }

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        return ResponseEntity.ok(Map.of(
                "totalIncome", totalIncome,
                "totalExpenses", totalExpenses,
                "netBalance", netBalance,
                "journalEntriesCount", entries.size()
        ));
    }

    @GetMapping("/journal-entries")
    @PreAuthorize("hasAnyRole('TREASURER', 'CLUB_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<JournalEntry>> getJournalEntries() {
        return ResponseEntity.ok(journalEntryRepository.findAll());
    }

    @PostMapping("/reimbursements")
    @Transactional
    public ResponseEntity<?> submitReimbursement(Authentication authentication, @RequestBody ReimbursementRequest request) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reimbursement reimbursement = Reimbursement.builder()
                .user(user)
                .description(request.getDescription())
                .amount(request.getAmount())
                .receiptUrl(request.getReceiptUrl())
                .status(Reimbursement.ReimbursementStatus.PENDING)
                .build();

        reimbursementRepository.save(reimbursement);
        return ResponseEntity.ok(reimbursement);
    }

    @GetMapping("/reimbursements")
    public ResponseEntity<List<Reimbursement>> getReimbursements() {
        return ResponseEntity.ok(reimbursementRepository.findAll());
    }

    @PatchMapping("/reimbursements/{id}/approve")
    @PreAuthorize("hasAnyRole('TREASURER', 'CLUB_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> approveReimbursement(@PathVariable Long id) {
        Reimbursement r = reimbursementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reimbursement not found"));

        r.setStatus(Reimbursement.ReimbursementStatus.APPROVED);
        reimbursementRepository.save(r);
        return ResponseEntity.ok(r);
    }

    @Data
    public static class ReimbursementRequest {
        private String description;
        private BigDecimal amount;
        private String receiptUrl;
    }
}
