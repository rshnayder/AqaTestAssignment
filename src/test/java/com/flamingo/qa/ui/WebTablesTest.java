package com.flamingo.qa.ui;

import com.flamingo.qa.ui.model.TableRecord;
import com.flamingo.qa.ui.page.WebTablesPage;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class WebTablesTest extends UiTestBase {

    @Test
    void shouldAddNewRecordAndFindItBySearch() {
        TableRecord record = uniqueRecord("Mira");
        WebTablesPage page = new WebTablesPage(page()).open();

        page.addRecord(record)
                .search(record.getEmail());

        assertSoftly(softly -> {
            softly.assertThat(page.table().visibleRowTexts()).hasSize(1);
            softly.assertThat(page.table().rowTextByEmail(record.getEmail()))
                    .contains(record.getFirstName(), record.getLastName(), record.getDepartment());
        });
    }

    @Test
    void shouldEditExistingRecord() {
        TableRecord record = uniqueRecord("Noah");
        String newDepartment = "Platform QA";
        WebTablesPage page = new WebTablesPage(page()).open()
                .addRecord(record)
                .editDepartment(record.getEmail(), newDepartment)
                .search(record.getEmail());

        assertSoftly(softly ->
                softly.assertThat(page.table().rowTextByEmail(record.getEmail())).contains(newDepartment));
    }

    @Test
    void shouldDeleteRecordAndRemoveItFromSearchResults() {
        TableRecord record = uniqueRecord("Iris");
        WebTablesPage page = new WebTablesPage(page()).open()
                .addRecord(record)
                .search(record.getEmail())
                .deleteRecord(record.getEmail());

        assertSoftly(softly -> {
            softly.assertThat(page.table().hasRecordWithEmail(record.getEmail())).isFalse();
            softly.assertThat(page.table().visibleRowTexts()).isEmpty();
        });
    }

    @Test
    void shouldFilterTableBySearchTerm() {
        TableRecord first = uniqueRecord("Aria");
        TableRecord second = uniqueRecord("Bryn");
        WebTablesPage page = new WebTablesPage(page()).open()
                .addRecord(first)
                .addRecord(second)
                .search(second.getEmail());

        assertSoftly(softly -> {
            softly.assertThat(page.table().visibleRowTexts()).hasSize(1);
            softly.assertThat(page.table().rowTextByEmail(second.getEmail())).contains(second.getFirstName());
        });
    }

    @Test
    void shouldShowMultipleAddedRecords() {
        TableRecord first = uniqueRecord("Zulu");
        TableRecord second = uniqueRecord("Alpha");
        WebTablesPage page = new WebTablesPage(page()).open()
                .addRecord(first)
                .addRecord(second);

        assertSoftly(softly -> softly.assertThat(page.table().firstNames())
                .contains(first.getFirstName(), second.getFirstName()));
    }

    private TableRecord uniqueRecord(String firstName) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return TableRecord.builder()
                .firstName(firstName + suffix.substring(0, 2))
                .lastName("Tester")
                .email("aqa-" + suffix + "@example.com")
                .age(31)
                .salary(90000)
                .department("Automation")
                .build();
    }
}
