package com.flamingo.qa.ui.page;

import com.flamingo.qa.ui.component.WebTableComponent;
import com.flamingo.qa.ui.model.TableRecord;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class WebTablesPage extends BasePage {

    private final WebTableComponent table;
    private final Locator addButton;
    private final Locator submitButton;
    private final Locator searchInput;
    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator emailInput;
    private final Locator ageInput;
    private final Locator salaryInput;
    private final Locator departmentInput;

    public WebTablesPage(Page page) {
        super(page);
        this.table = new WebTableComponent(page);
        this.addButton = page.locator("#addNewRecordButton");
        this.submitButton = page.locator("#submit");
        this.searchInput = page.locator("#searchBox");
        this.firstNameInput = page.locator("#firstName");
        this.lastNameInput = page.locator("#lastName");
        this.emailInput = page.locator("#userEmail");
        this.ageInput = page.locator("#age");
        this.salaryInput = page.locator("#salary");
        this.departmentInput = page.locator("#department");
    }

    @Step("Open Web Tables page")
    public WebTablesPage open() {
        navigate("/webtables");
        waitVisible(addButton);
        return this;
    }

    public WebTablesPage addRecord(TableRecord record) {
        return addRecord(record.getEmail(), record.getFirstName(), record.getLastName(), record);
    }

    @Step("Add table record: email={0}, name={1} {2}")
    private WebTablesPage addRecord(String email, String firstName, String lastName, TableRecord record) {
        click(addButton);
        fillForm(record);
        click(submitButton);
        table.waitUntilRecordVisible(email);
        return this;
    }

    @Step("Edit table record department: email={0}, department={1}")
    public WebTablesPage editDepartment(String email, String department) {
        table.clickEditForEmail(email);
        fill(departmentInput, department);
        click(submitButton);
        table.waitUntilRecordVisible(email);
        return this;
    }

    @Step("Delete table record: email={0}")
    public WebTablesPage deleteRecord(String email) {
        table.clickDeleteForEmail(email);
        table.waitUntilRecordHidden(email);
        return this;
    }

    @Step("Search web table: {0}")
    public WebTablesPage search(String text) {
        fill(searchInput, text);
        return this;
    }

    public WebTableComponent table() {
        return table;
    }

    @Step("Fill web table form")
    private void fillForm(TableRecord record) {
        fill(firstNameInput, record.getFirstName());
        fill(lastNameInput, record.getLastName());
        fill(emailInput, record.getEmail());
        fill(ageInput, String.valueOf(record.getAge()));
        fill(salaryInput, String.valueOf(record.getSalary()));
        fill(departmentInput, record.getDepartment());
    }
}
