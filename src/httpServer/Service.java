package httpServer;

import idNumber.EEIdNumber;
import salary.GrossSalary;
import salary.SalaryCalculator;

import java.io.IOException;
import java.math.BigDecimal;

public class Service {

    public Service() {
    }

    public String validateIdCode(String id) {
        EEIdNumber eeIdNumber = new EEIdNumber(id);
        return "{\"Validation\":\""+ eeIdNumber.validateIdNumber() + "\",\"BirthDate\":\"" + eeIdNumber.birthDate + "\"}";
    }

    public String calculateSalary(String salary) throws IOException {
        SalaryCalculator calculation = new SalaryCalculator(new GrossSalary(BigDecimal.valueOf(Long.parseLong(salary))), false, true, true, true, true);
        return calculation.toString();
    }
}

