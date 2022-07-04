package salary;

import java.math.BigDecimal;

public class GrossSalary extends Salary {

    public GrossSalary(BigDecimal salary) {
        this.grossSalary = salary;
    }

    @Override
    BigDecimal getGross(boolean useSocialTaxMinRate, boolean useTaxFreeMin, boolean useEmployerUnemploymentInsurance, boolean useEmployeeUnemploymentInsurance, boolean usePension) {
        return grossSalary;
    }
}
