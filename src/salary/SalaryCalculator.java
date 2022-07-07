package salary;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

@AllArgsConstructor
public class SalaryCalculator extends Salary {

    @Override
    BigDecimal getGross(boolean useSocialTaxMinRate, boolean useTaxFreeMin, boolean useEmployerUnemploymentInsurance, boolean useEmployeeUnemploymentInsurance, boolean usePension) {
        return grossSalary;
    }

    public SalaryCalculator(Salary salary, boolean useSocialTaxMinRate, boolean useTaxFreeMin, boolean useEmployerUnemploymentInsurance, boolean useEmployeeUnemploymentInsurance, boolean usePension) {
        this.useSocialTaxMinRate = useSocialTaxMinRate;
        this.useTaxFreeMin = useTaxFreeMin;
        this.useEmployerUnemploymentInsurance = useEmployerUnemploymentInsurance;
        this.useEmployeeUnemploymentInsurance = useEmployeeUnemploymentInsurance;
        this.usePension = usePension;
        grossSalary = salary.getGross(useSocialTaxMinRate, useTaxFreeMin, useEmployerUnemploymentInsurance, useEmployeeUnemploymentInsurance, usePension);
        calculateTaxesFromGross();
    }

    private BigDecimal getGrossTaxFreeMonthly() {
        BigDecimal taxFreeMonthly;
        BigDecimal monthsInYear = d(12);
        BigDecimal maxTaxFreeMonthly = d(500);
        BigDecimal yearlyIncome = grossSalary.multiply(monthsInYear);
        BigDecimal yearlyIncomeMin = d(14400);
        BigDecimal yearlyIncomeMax = d(25200);
        BigDecimal yearlyTaxFreeIncomeMax = maxTaxFreeMonthly.add(pension).add(employeeUnemploymentInsurance).multiply(monthsInYear);

        if (!useTaxFreeMin) {
            taxFreeMonthly = d(0);
        } else if (yearlyIncome.compareTo(yearlyTaxFreeIncomeMax) > 0 && yearlyIncome.compareTo(yearlyIncomeMin) <= 0) {
            taxFreeMonthly = maxTaxFreeMonthly;
        } else if (yearlyIncome.compareTo(yearlyIncomeMin) > 0 && yearlyIncome.compareTo(yearlyIncomeMax) < 0) {
            taxFreeMonthly = getDynamicTaxFreeMonthly(monthsInYear, yearlyIncome, yearlyIncomeMin, yearlyIncomeMax, maxTaxFreeMonthly);
        } else if (yearlyIncome.compareTo(yearlyIncomeMax) >= 0) {
            taxFreeMonthly = d(0);
        } else {
            taxFreeMonthly = grossSalary.subtract(employeeUnemploymentInsurance).subtract(pension).setScale(2, HALF_UP);
        }
        return taxFreeMonthly;
    }

    private void calculatePension() {
        if (usePension) {
            pension = calculatePercent(grossSalary, SECOND_PILLAR_PENSION).setScale(2, HALF_UP);
        } else {
            pension = d(0.00).setScale(2, HALF_UP);
        }
    }

    private void calculateEmployeeUnemploymentInsurance() {
        if (useEmployeeUnemploymentInsurance) {
            employeeUnemploymentInsurance = calculatePercent(grossSalary, UNEMPLOYMENT_INSURANCE_EMPLOYEE).setScale(2, HALF_UP);
        } else {
            employeeUnemploymentInsurance = d(0.00).setScale(2, HALF_UP);
        }
    }

    private void calculateEmployerUnemploymentInsurance() {
        if (useEmployerUnemploymentInsurance) {
            employerUnemploymentInsurance = calculatePercent(grossSalary, UNEMPLOYMENT_INSURANCE_EMPLOYER).setScale(2, HALF_UP);
        } else {
            employerUnemploymentInsurance = d(0.00);
        }
    }

    private void calculateSocialTax() {
        if (useSocialTaxMinRate) {
            socialTax = d(192.72);
        } else {
            socialTax = calculatePercent(grossSalary, SOCIAL_TAX).setScale(2, HALF_UP);
        }
    }

    void calculateTaxesFromGross() {
        calculateSocialTax();
        calculateEmployerUnemploymentInsurance();
        totalSalary = grossSalary.add(socialTax).add(employerUnemploymentInsurance).setScale(2, HALF_UP);
        calculateEmployeeUnemploymentInsurance();
        calculatePension();
        incomeTax = calculatePercent(grossSalary.subtract(employeeUnemploymentInsurance).subtract(pension).subtract(getGrossTaxFreeMonthly()), INCOME_TAX).setScale(2, HALF_UP);
        netSalary = grossSalary.subtract(employeeUnemploymentInsurance).subtract(pension).subtract(incomeTax).setScale(2, HALF_UP);
    }
}
