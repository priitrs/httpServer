package salary;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

public class NetSalary extends Salary {
    private final BigDecimal netSalary;

    public NetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    @Override
    BigDecimal getGross(boolean useSocialTaxMinRate, boolean useTaxFreeMin, boolean useEmployerUnemploymentInsurance, boolean useEmployeeUnemploymentInsurance, boolean usePension) {
        this.useSocialTaxMinRate = useSocialTaxMinRate;
        this.useTaxFreeMin = useTaxFreeMin;
        this.useEmployerUnemploymentInsurance = useEmployerUnemploymentInsurance;
        this.useEmployeeUnemploymentInsurance = useEmployeeUnemploymentInsurance;
        this.usePension = usePension;

        return getGrossSalaryFromNet(netSalary);
    }

    private BigDecimal getGrossSalaryFromNet(BigDecimal salary) {
        if (!usePension) {
            SECOND_PILLAR_PENSION = d(0);
        }
         if (!useEmployeeUnemploymentInsurance) {
            UNEMPLOYMENT_INSURANCE_EMPLOYEE = d(0);
        }
        return getTotalValueFromPercent(getTotalValueFromPercent(salary.subtract(getNetTaxFreeMonthly(salary)), d(100).subtract(INCOME_TAX))
                .add(getNetTaxFreeMonthly(salary)), d(100).subtract(SECOND_PILLAR_PENSION).subtract(UNEMPLOYMENT_INSURANCE_EMPLOYEE)).setScale(2, HALF_UP);
    }

    private BigDecimal getNetTaxFreeMonthly(BigDecimal salary) {
        BigDecimal taxFreeMonthly;
        BigDecimal monthsInYear = d(12);
        BigDecimal maxTaxFreeMonthly = d(500);
        BigDecimal yearlyIncome = salary.multiply(monthsInYear);

        BigDecimal lowerLimitMonthly = new SalaryCalculator(new GrossSalary(d(1200)), useSocialTaxMinRate, useTaxFreeMin, useEmployerUnemploymentInsurance, useEmployeeUnemploymentInsurance, usePension).netSalary;
        BigDecimal upperLimitMonthly = new SalaryCalculator(new GrossSalary(d(2100)), useSocialTaxMinRate, useTaxFreeMin, useEmployerUnemploymentInsurance, useEmployeeUnemploymentInsurance, usePension).netSalary;

        BigDecimal yearlyIncomeMin = lowerLimitMonthly.multiply(monthsInYear);
        BigDecimal yearlyIncomeMax = upperLimitMonthly.multiply(monthsInYear);

        BigDecimal yearlyTaxFreeIncomeMax = maxTaxFreeMonthly.multiply(monthsInYear);

        if (!useTaxFreeMin) {
            taxFreeMonthly = d(0);
        }else if (yearlyIncome.compareTo(yearlyTaxFreeIncomeMax) > 0 && yearlyIncome.compareTo(yearlyIncomeMin) <= 0) {
            taxFreeMonthly = maxTaxFreeMonthly;
        } else if (yearlyIncome.compareTo(yearlyIncomeMin) > 0 && yearlyIncome.compareTo(yearlyIncomeMax) < 0) {
            taxFreeMonthly = getDynamicTaxFreeMonthly(monthsInYear, yearlyIncome, yearlyIncomeMin, yearlyIncomeMax, maxTaxFreeMonthly);
        } else if (yearlyIncome.compareTo(yearlyIncomeMax) > 0) {
            taxFreeMonthly = d(0);
        } else {
            taxFreeMonthly = salary;
        }

        return taxFreeMonthly;
    }
}
