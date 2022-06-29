package idNumber;

public class IdNumberData {
    int century;
    int year;
    int month;
    int day;
    String hypen;
    int birthCounty;
    int gender;
    int serialNumber;
    int controlNumber;

    public IdNumberData() {
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getCentury() {
        return century;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getHypen() {
        return hypen;
    }

    public int getBirthCounty() {
        return birthCounty;
    }

    public int getGender() {
        return gender;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public int getControlNumber() {
        return controlNumber;
    }
}
