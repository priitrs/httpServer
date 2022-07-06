package httpServer;

import idNumber.EEIdNumber;
import salary.GrossSalary;
import salary.NetSalary;
import salary.SalaryCalculator;
import salary.TotalSalary;

import java.io.IOException;
import java.math.BigDecimal;

public class Service {

    public Service() {
    }

    public String validateIdCode(String id) {
        EEIdNumber eeIdNumber = new EEIdNumber(id);
        return "{\"Validation\":\"" + eeIdNumber.validateIdNumber() + "\",\"BirthDate\":\"" + eeIdNumber.birthDate + "\"}";
    }

    public String calculateSalary(String salary, String type) throws IOException {
        switch (type) {
            case "gross":
                SalaryCalculator gross = new SalaryCalculator(new GrossSalary(BigDecimal.valueOf(Long.parseLong(salary))), false, true, true, true, true);
                return gross.toString();
            case "net":
                SalaryCalculator netToGross = new SalaryCalculator(new NetSalary(BigDecimal.valueOf(Long.parseLong(salary))), false, true, true, true, true);
                return netToGross.toString();
            case "total":
                SalaryCalculator totalToGross = new SalaryCalculator(new TotalSalary(BigDecimal.valueOf(Long.parseLong(salary))), false, true, true, true, true);
                return totalToGross.toString();
        }
        return "";
    }
}

