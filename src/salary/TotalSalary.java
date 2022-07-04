package salary;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

public class TotalSalary extends Salary{

    BigDecimal totalSalary;

    public TotalSalary(BigDecimal salary) {
        this.totalSalary = salary;
    }

    @Override
    BigDecimal getGross(boolean useSocialTaxMinRate, boolean useTaxFreeMin, boolean useEmployerUnemploymentInsurance, boolean useEmployeeUnemploymentInsurance, boolean usePension) {
        this.useSocialTaxMinRate = useSocialTaxMinRate;
        this.useTaxFreeMin = useTaxFreeMin;
        this.useEmployerUnemploymentInsurance = useEmployerUnemploymentInsurance;
        this.useEmployeeUnemploymentInsurance = useEmployeeUnemploymentInsurance;
        this.usePension = usePension;
        return getTotalValueFromPercent(totalSalary, d(100).add(SOCIAL_TAX).add(UNEMPLOYMENT_INSURANCE_EMPLOYER)).setScale(2, HALF_UP);
    }
}
