package com.flamingo.qa.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class WebTableComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebTableComponent.class);
    private static final double TABLE_WAIT_TIMEOUT_MS = 8_000;

    private final Locator rows;

    public WebTableComponent(Page page) {
        this.rows = page.getByRole(AriaRole.ROW);
    }

    public boolean hasRecordWithEmail(String email) {
        boolean found = rowIndexByEmail(email) >= 0;
        LOGGER.debug("Record lookup by email={} found={}", email, found);
        return found;
    }

    public String rowTextByEmail(String email) {
        Locator row = rowByEmail(email);
        return row.innerText();
    }

    @Step("Wait until table record is visible: email={0}")
    public void waitUntilRecordVisible(String email) {
        assertThat(rowLocatorByEmail(email))
                .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(TABLE_WAIT_TIMEOUT_MS));
    }

    @Step("Wait until table record is hidden: email={0}")
    public void waitUntilRecordHidden(String email) {
        assertThat(rowLocatorByEmail(email))
                .isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(TABLE_WAIT_TIMEOUT_MS));
    }

    @Step("Click edit action for email={0}")
    public void clickEditForEmail(String email) {
        rowByEmail(email).locator("[title='Edit']").click();
    }

    @Step("Click delete action for email={0}")
    public void clickDeleteForEmail(String email) {
        rowByEmail(email).locator("[title='Delete']").click();
    }

    public List<String> visibleRowTexts() {
        List<String> visibleRows = rows()
                .allTextContents()
                .stream()
                .skip(1)
                .map(WebTableComponent::trimToEmpty)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
        LOGGER.debug("Visible table rows: count={} rows={}", visibleRows.size(), visibleRows);
        return visibleRows;
    }

    public List<String> firstNames() {
        return rows().all()
                .stream()
                .skip(1)
                .map(row -> trimToEmpty(row.getByRole(AriaRole.CELL).first().textContent()))
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private Locator rowByEmail(String email) {
        Locator row = rowLocatorByEmail(email).first();
        assertThat(row).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(TABLE_WAIT_TIMEOUT_MS));
        return row;
    }

    private int rowIndexByEmail(String email) {
        String expected = normalizeForLookup(email);
        if (expected.isEmpty()) {
            return -1;
        }
        List<String> rows = rows().allTextContents();
        for (int index = 0; index < rows.size(); index++) {
            if (normalizeForLookup(rows.get(index)).contains(expected)) {
                return index;
            }
        }
        return -1;
    }

    private Locator rows() {
        return rows;
    }

    private Locator rowLocatorByEmail(String email) {
        return rows().filter(new Locator.FilterOptions().setHasText(email));
    }

    private static String normalizeForLookup(String value) {
        return trimToEmpty(value).replaceAll("\\s+", "");
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
