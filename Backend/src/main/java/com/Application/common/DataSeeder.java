package com.skyline.common;

import com.skyline.auth.model.Role;
import com.skyline.auth.model.User;
import com.skyline.auth.repository.UserRepository;
import com.skyline.event.model.Event;
import com.skyline.event.model.TicketCategory;
import com.skyline.event.repository.EventRepository;
import com.skyline.finance.model.Account;
import com.skyline.finance.model.JournalEntry;
import com.skyline.finance.model.JournalLine;
import com.skyline.finance.repository.AccountRepository;
import com.skyline.finance.repository.JournalEntryRepository;
import com.skyline.membership.model.Membership;
import com.skyline.membership.repository.MembershipRepository;
import com.skyline.merchandise.model.MerchandiseProduct;
import com.skyline.merchandise.model.ProductVariant;
import com.skyline.merchandise.repository.MerchandiseProductRepository;
import com.skyline.volunteer.model.Task;
import com.skyline.volunteer.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipRepository membershipRepository;
    private final EventRepository eventRepository;
    private final MerchandiseProductRepository productRepository;
    private final TaskRepository taskRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding complete Skyline domain data with INR currencies and Balaji S. accounts...");

            String defaultPassword = passwordEncoder.encode("Password@123");

            // Seed Demo Accounts using simple names: 1 Ajay, 2 Arun, 3 Nithish, 4 Mani, 5 Ramesh
            // 1. STUDENT Role (5 Accounts)
            User s1 = User.builder().fullName("Ajay").email("student1.demo@gmail.com").studentId("20260001").password(defaultPassword).phone("+91 98765 43210").role(Role.STUDENT).createdAt(LocalDateTime.now()).build();
            User s2 = User.builder().fullName("Arun").email("student2.demo@gmail.com").studentId("20260012").password(defaultPassword).phone("+91 98765 43211").role(Role.STUDENT).createdAt(LocalDateTime.now()).build();
            User s3 = User.builder().fullName("Nithish").email("student3.demo@gmail.com").studentId("20260013").password(defaultPassword).phone("+91 98765 43212").role(Role.STUDENT).createdAt(LocalDateTime.now()).build();
            User s4 = User.builder().fullName("Mani").email("student4.demo@gmail.com").studentId("20260014").password(defaultPassword).phone("+91 98765 43213").role(Role.STUDENT).createdAt(LocalDateTime.now()).build();
            User s5 = User.builder().fullName("Ramesh").email("student5.demo@gmail.com").studentId("20260015").password(defaultPassword).phone("+91 98765 43214").role(Role.STUDENT).createdAt(LocalDateTime.now()).build();

            // 2. VOLUNTEER Role with Points (5 Accounts)
            User v1 = User.builder().fullName("Ajay").email("volunteer1.demo@gmail.com").studentId("20260005").password(defaultPassword).phone("+91 98765 43250").role(Role.VOLUNTEER).volunteerPoints(120).createdAt(LocalDateTime.now()).build();
            User v2 = User.builder().fullName("Arun").email("volunteer2.demo@gmail.com").studentId("20260052").password(defaultPassword).phone("+91 98765 43251").role(Role.VOLUNTEER).volunteerPoints(80).createdAt(LocalDateTime.now()).build();
            User v3 = User.builder().fullName("Nithish").email("volunteer3.demo@gmail.com").studentId("20260053").password(defaultPassword).phone("+91 98765 43252").role(Role.VOLUNTEER).volunteerPoints(50).createdAt(LocalDateTime.now()).build();
            User v4 = User.builder().fullName("Mani").email("volunteer4.demo@gmail.com").studentId("20260054").password(defaultPassword).phone("+91 98765 43253").role(Role.VOLUNTEER).volunteerPoints(30).createdAt(LocalDateTime.now()).build();
            User v5 = User.builder().fullName("Ramesh").email("volunteer5.demo@gmail.com").studentId("20260055").password(defaultPassword).phone("+91 98765 43254").role(Role.VOLUNTEER).volunteerPoints(10).createdAt(LocalDateTime.now()).build();

            // 3. EVENT_COORDINATOR Role (5 Accounts)
            User o1 = User.builder().fullName("Ajay").email("eventcoord1.demo@gmail.com").studentId("20260003").password(defaultPassword).phone("+91 98765 43230").role(Role.EVENT_COORDINATOR).createdAt(LocalDateTime.now()).build();
            User o2 = User.builder().fullName("Arun").email("eventcoord2.demo@gmail.com").studentId("20260032").password(defaultPassword).phone("+91 98765 43231").role(Role.EVENT_COORDINATOR).createdAt(LocalDateTime.now()).build();
            User o3 = User.builder().fullName("Nithish").email("eventcoord3.demo@gmail.com").studentId("20260033").password(defaultPassword).phone("+91 98765 43232").role(Role.EVENT_COORDINATOR).createdAt(LocalDateTime.now()).build();
            User o4 = User.builder().fullName("Mani").email("eventcoord4.demo@gmail.com").studentId("20260034").password(defaultPassword).phone("+91 98765 43233").role(Role.EVENT_COORDINATOR).createdAt(LocalDateTime.now()).build();
            User o5 = User.builder().fullName("Ramesh").email("eventcoord5.demo@gmail.com").studentId("20260035").password(defaultPassword).phone("+91 98765 43234").role(Role.EVENT_COORDINATOR).createdAt(LocalDateTime.now()).build();

            // 4. SUPER_ADMIN Role (1 Account)
            User superAdmin = User.builder().fullName("Ramesh").email("superadmin.demo@gmail.com").studentId("20260006").password(defaultPassword).phone("+91 98765 43215").role(Role.SUPER_ADMIN).createdAt(LocalDateTime.now()).build();


            userRepository.saveAll(List.of(
                s1, s2, s3, s4, s5,
                v1, v2, v3, v4, v5,
                o1, o2, o3, o4, o5,
                superAdmin
            ));

            // Seed Active Memberships for Student Demo Accounts (INR 500 annual dues)
            Membership m1 = Membership.builder().user(s1).startDate(LocalDate.now().minusMonths(2)).expiryDate(LocalDate.now().plusMonths(10)).duesPaid(new BigDecimal("500.00")).status(Membership.MembershipStatus.ACTIVE).build();
            Membership m2 = Membership.builder().user(s2).startDate(LocalDate.now().minusMonths(1)).expiryDate(LocalDate.now().plusMonths(11)).duesPaid(new BigDecimal("500.00")).status(Membership.MembershipStatus.ACTIVE).build();
            Membership m3 = Membership.builder().user(s3).startDate(LocalDate.now().minusMonths(3)).expiryDate(LocalDate.now().plusMonths(9)).duesPaid(new BigDecimal("500.00")).status(Membership.MembershipStatus.ACTIVE).build();
            membershipRepository.saveAll(List.of(m1, m2, m3));

            // Seed Spring Gala Event (INR 250 Member / INR 600 Non-Member)
            Event springGala = Event.builder()
                    .title("Annual Spring Gala 2026")
                    .description("Campus annual gala celebration, dining, networking, and student leadership awards.")
                    .venue("Grand Campus Auditorium & Lawns")
                    .startTime(LocalDateTime.now().plusDays(14))
                    .endTime(LocalDateTime.now().plusDays(14).plusHours(4))
                    .totalCapacity(150)
                    .status(Event.EventStatus.PUBLISHED)
                    .imageUrl("https://images.unsplash.com/photo-1511795409834-ef04bbd61622")
                    .build();

            TicketCategory galaVIP = TicketCategory.builder()
                    .event(springGala)
                    .name("General Gala Entry Ticket")
                    .memberPrice(new BigDecimal("250.00"))
                    .nonMemberPrice(new BigDecimal("600.00"))
                    .totalQuantity(150)
                    .reservedQuantity(0)
                    .soldQuantity(12)
                    .build();

            springGala.setCategories(List.of(galaVIP));
            eventRepository.save(springGala);

            // Seed Merchandise Products (INR 1299 Member / INR 1799 Non-Member)
            MerchandiseProduct hoodie = MerchandiseProduct.builder()
                    .name("Student Association Crest Hoodie")
                    .description("Heavyweight 350GSM cotton fleece hoodie with embroidered emblem.")
                    .price(new BigDecimal("1799.00"))
                    .memberPrice(new BigDecimal("1299.00"))
                    .imageUrl("https://images.unsplash.com/photo-1556905055-8f358a7a47b2")
                    .build();

            ProductVariant hS = ProductVariant.builder().product(hoodie).size("S").stockQuantity(15).build();
            ProductVariant hM = ProductVariant.builder().product(hoodie).size("M").stockQuantity(25).build();
            ProductVariant hL = ProductVariant.builder().product(hoodie).size("L").stockQuantity(20).build();
            ProductVariant hXL = ProductVariant.builder().product(hoodie).size("XL").stockQuantity(10).build();

            hoodie.setVariants(List.of(hS, hM, hL, hXL));
            productRepository.save(hoodie);

            // Seed Volunteer Tasks
            Task task1 = Task.builder()
                    .event(springGala)
                    .title("Bake Sale Refreshments & Supplies")
                    .description("Coordinate bake sale table items and supply purchase.")
                    .assignedUser(v1)
                    .priority(Task.TaskPriority.HIGH)
                    .status(Task.TaskStatus.IN_PROGRESS)
                    .dueDate(LocalDate.now().plusDays(3))
                    .build();

            Task task2 = Task.builder()
                    .event(springGala)
                    .title("Door Registration & QR Scanner Desk")
                    .description("Set up fast QR scanner desk at Auditorium Entrance.")
                    .assignedUser(v1)
                    .priority(Task.TaskPriority.MEDIUM)
                    .status(Task.TaskStatus.TO_DO)
                    .dueDate(LocalDate.now().plusDays(13))
                    .build();

            taskRepository.saveAll(List.of(task1, task2));

            // Seed Double-Entry Accounts & Initial Journal Ledger Entry (in INR ₹)
            Account bankAccount = Account.builder().accountNumber("1010").name("Cash in Bank / Payment Gateway").type(Account.AccountType.ASSET).build();
            Account revenueDues = Account.builder().accountNumber("4010").name("Membership Dues Revenue").type(Account.AccountType.REVENUE).build();
            Account revenueTickets = Account.builder().accountNumber("4020").name("Event Ticket Sales Revenue").type(Account.AccountType.REVENUE).build();

            accountRepository.saveAll(List.of(bankAccount, revenueDues, revenueTickets));

            JournalEntry initialEntry = JournalEntry.builder()
                    .referenceNumber("JE-2026-001")
                    .description("Initial Semester Dues & Ticket Sales Collection")
                    .entryDate(LocalDate.now())
                    .build();

            JournalLine line1 = JournalLine.builder().journalEntry(initialEntry).account(bankAccount).debit(new BigDecimal("48500.00")).credit(BigDecimal.ZERO).build();
            JournalLine line2 = JournalLine.builder().journalEntry(initialEntry).account(revenueDues).debit(BigDecimal.ZERO).credit(new BigDecimal("35000.00")).build();
            JournalLine line3 = JournalLine.builder().journalEntry(initialEntry).account(revenueTickets).debit(BigDecimal.ZERO).credit(new BigDecimal("13500.00")).build();

            initialEntry.setLines(List.of(line1, line2, line3));
            journalEntryRepository.save(initialEntry);

            log.info("Successfully seeded Skyline dataset with INR values and Balaji S. accounts!");
        }
    }
}
