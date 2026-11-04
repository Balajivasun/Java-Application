package com.skyline.intelligence.controller;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/intelligence")
public class BreakEvenCalculatorController {

    @PostMapping("/break-even")
    public ResponseEntity<?> calculateBreakEven(@RequestBody BreakEvenRequest request) {
        BigDecimal fixedCosts = request.getFixedCosts() != null ? request.getFixedCosts() : BigDecimal.ZERO;
        BigDecimal variableCostPerHead = request.getVariableCostPerHead() != null ? request.getVariableCostPerHead() : BigDecimal.ZERO;
        BigDecimal ticketPrice = request.getTicketPrice() != null ? request.getTicketPrice() : BigDecimal.ONE;

        // Contribution Margin = Ticket Price - Variable Cost Per Head
        BigDecimal contributionMargin = ticketPrice.subtract(variableCostPerHead);

        int breakEvenAttendance = 0;
        if (contributionMargin.compareTo(BigDecimal.ZERO) > 0) {
            breakEvenAttendance = fixedCosts.divide(contributionMargin, 0, RoundingMode.CEILING).intValue();
        }

        int expectedAttendance = request.getExpectedAttendance() != null ? request.getExpectedAttendance() : 100;
        BigDecimal totalExpectedRevenue = ticketPrice.multiply(new BigDecimal(expectedAttendance));
        BigDecimal totalExpectedCosts = fixedCosts.add(variableCostPerHead.multiply(new BigDecimal(expectedAttendance)));
        BigDecimal expectedSurplus = totalExpectedRevenue.subtract(totalExpectedCosts);

        return ResponseEntity.ok(Map.of(
                "breakEvenAttendance", breakEvenAttendance,
                "contributionMarginPerTicket", contributionMargin,
                "expectedAttendance", expectedAttendance,
                "totalExpectedRevenue", totalExpectedRevenue,
                "totalExpectedCosts", totalExpectedCosts,
                "expectedSurplusOrDeficit", expectedSurplus,
                "isProfitable", expectedSurplus.compareTo(BigDecimal.ZERO) >= 0
        ));
    }

    @Data
    public static class BreakEvenRequest {
        private BigDecimal fixedCosts; // Venue, Printing, DJ
        private BigDecimal variableCostPerHead; // Catering, Badge per attendee
        private BigDecimal ticketPrice;
        private Integer expectedAttendance;
    }
}
