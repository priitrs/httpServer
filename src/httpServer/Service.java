package httpServer;

import idNumber.EEIdNumber;

public class Service {

    public Service() {
    }

    public String validateIdCode(String id) {
        EEIdNumber eeIdNumber = new EEIdNumber(id);
        return eeIdNumber.validateIdNumber() + " " + eeIdNumber.birthDate;
    }

    public String calculateSalary(String grossSalary) {
        return "some result from " + grossSalary;
    }
}
