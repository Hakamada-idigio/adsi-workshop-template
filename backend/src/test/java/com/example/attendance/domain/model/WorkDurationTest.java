package com.example.attendance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkDurationTest {

    @Test
    @DisplayName("出勤〜退勤で休憩なしの場合、実労働時間が正確に計算される")
    void calculate_noBreaks_returnsCorrectDuration() {
        LocalDateTime clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2026, 7, 14, 18, 0);

        WorkDuration duration = WorkDuration.calculate(clockIn, clockOut, List.of());

        assertThat(duration.totalMinutes()).isEqualTo(540);
        assertThat(duration.breakMinutes()).isEqualTo(0);
        assertThat(duration.netWorkingMinutes()).isEqualTo(540);
    }

    @Test
    @DisplayName("休憩ありの場合、休憩時間が差し引かれる")
    void calculate_withBreaks_subtractsBreakTime() {
        LocalDateTime clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2026, 7, 14, 18, 0);
        BreakRecord breakRecord = BreakRecord.builder()
                .breakStart(LocalDateTime.of(2026, 7, 14, 12, 0))
                .breakEnd(LocalDateTime.of(2026, 7, 14, 13, 0))
                .build();

        WorkDuration duration = WorkDuration.calculate(clockIn, clockOut, List.of(breakRecord));

        assertThat(duration.totalMinutes()).isEqualTo(540);
        assertThat(duration.breakMinutes()).isEqualTo(60);
        assertThat(duration.netWorkingMinutes()).isEqualTo(480);
    }

    @Test
    @DisplayName("複数回の休憩がある場合、合計が正しい")
    void calculate_multipleBreaks_sumsCorrectly() {
        LocalDateTime clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        LocalDateTime clockOut = LocalDateTime.of(2026, 7, 14, 18, 0);
        BreakRecord break1 = BreakRecord.builder()
                .breakStart(LocalDateTime.of(2026, 7, 14, 12, 0))
                .breakEnd(LocalDateTime.of(2026, 7, 14, 12, 45))
                .build();
        BreakRecord break2 = BreakRecord.builder()
                .breakStart(LocalDateTime.of(2026, 7, 14, 15, 0))
                .breakEnd(LocalDateTime.of(2026, 7, 14, 15, 15))
                .build();

        WorkDuration duration = WorkDuration.calculate(clockIn, clockOut, List.of(break1, break2));

        assertThat(duration.totalMinutes()).isEqualTo(540);
        assertThat(duration.breakMinutes()).isEqualTo(60);
        assertThat(duration.netWorkingMinutes()).isEqualTo(480);
    }

    @Test
    @DisplayName("1分単位で正確に計算される（丸めなし）")
    void calculate_oddMinutes_noRounding() {
        LocalDateTime clockIn = LocalDateTime.of(2026, 7, 14, 9, 7);
        LocalDateTime clockOut = LocalDateTime.of(2026, 7, 14, 17, 23);
        BreakRecord breakRecord = BreakRecord.builder()
                .breakStart(LocalDateTime.of(2026, 7, 14, 12, 3))
                .breakEnd(LocalDateTime.of(2026, 7, 14, 12, 47))
                .build();

        WorkDuration duration = WorkDuration.calculate(clockIn, clockOut, List.of(breakRecord));

        assertThat(duration.totalMinutes()).isEqualTo(496);
        assertThat(duration.breakMinutes()).isEqualTo(44);
        assertThat(duration.netWorkingMinutes()).isEqualTo(452);
    }
}
