package salary;

import java.math.BigDecimal;
import java.util.Objects;

import static java.math.RoundingMode.HALF_UP;

public abstract class Salary {

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    BigDecimal socialTax;
    BigDecimal employerUnemploymentInsurance;
    BigDecimal employeeUnemploymentInsurance;
    BigDecimal pension;
    BigDecimal incomeTax;
    BigDecimal totalSalary;
    BigDecimal grossSalary;
    BigDecimal netSalary;
    boolean useSocialTaxMinRate;
    boolean useTaxFreeMin;
    boolean useEmployerUnemploymentInsurance;
    boolean useEmployeeUnemploymentInsurance;
    boolean usePension;

    final BigDecimal SOCIAL_TAX = new BigDecimal("33.00");
    final BigDecimal UNEMPLOYMENT_INSURANCE_EMPLOYER = new BigDecimal("0.80");
    BigDecimal SECOND_PILLAR_PENSION = new BigDecimal("2.00");
    BigDecimal UNEMPLOYMENT_INSURANCE_EMPLOYEE = new BigDecimal("1.60");
    final BigDecimal INCOME_TAX = new BigDecimal("20.00");

    public Salary() {
    }

    abstract BigDecimal getGross(boolean useSocialTaxMinRate, boolean useTaxFreeMin, boolean useEmployerUnemploymentInsurance, boolean useEmployeeUnemploymentInsurance, boolean usePension);

    BigDecimal getDynamicTaxFreeMonthly(BigDecimal monthsInYear, BigDecimal yearlyIncome, BigDecimal yearlyIncomeMin, BigDecimal yearlyIncomeMax, BigDecimal maxTaxFreeMonthly) {
        BigDecimal taxFreeMonthly;
        BigDecimal incomeExceedingTaxFreeAmount = yearlyIncome.subtract(yearlyIncomeMin);
        BigDecimal yearlyTaxFreeIncome = maxTaxFreeMonthly.multiply(monthsInYear);
        taxFreeMonthly = (yearlyTaxFreeIncome.subtract(yearlyTaxFreeIncome
                .divide(yearlyIncomeMax.subtract(yearlyIncomeMin), 10, HALF_UP)
                .multiply(incomeExceedingTaxFreeAmount))).divide(monthsInYear, 2, HALF_UP);
        return taxFreeMonthly;
    }

    BigDecimal calculatePercent(BigDecimal value, BigDecimal percent) {
        return value.multiply(percent.divide(d(100), 10, HALF_UP));
    }

    BigDecimal getTotalValueFromPercent(BigDecimal value, BigDecimal percent) {
        return value.divide(percent, 10, HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    BigDecimal d(double value) {
        return BigDecimal.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Salary salary = (Salary) o;
        return Objects.equals(socialTax, salary.socialTax) && Objects.equals(employerUnemploymentInsurance, salary.employerUnemploymentInsurance) && Objects.equals(employeeUnemploymentInsurance, salary.employeeUnemploymentInsurance) && Objects.equals(pension, salary.pension) && Objects.equals(incomeTax, salary.incomeTax) && Objects.equals(totalSalary, salary.totalSalary) && Objects.equals(grossSalary, salary.grossSalary) && Objects.equals(netSalary, salary.netSalary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socialTax, employerUnemploymentInsurance, employeeUnemploymentInsurance, pension, incomeTax, totalSalary, grossSalary, netSalary);
    }

    @Override
    public String toString() {
        return "{" +
                "\"socialTax\":" + "\"" + socialTax + "\"" +
                ",\"employerUnemploymentInsurance\":" + "\"" + employerUnemploymentInsurance + "\"" +
                ",\"employeeUnemploymentInsurance\":" + "\"" + employeeUnemploymentInsurance + "\"" +
                ",\"pension\":" + "\"" + pension + "\"" +
                ",\"incomeTax\":" + "\"" + incomeTax + "\"" +
                ",\"totalSalary\":" + "\"" + totalSalary + "\"" +
                ",\"grossSalary\":" + "\"" + grossSalary + "\"" +
                ",\"netSalary\":" + "\"" + netSalary + "\"" +
                '}';
    }
}
