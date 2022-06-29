package idNumber;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;

public abstract class IdNumber {

    public String idNumber;
    public IdNumberData idNumberData = new IdNumberData();
    public LocalDate birthDate;

    public IdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public boolean validateIdNumber() {
        if (isLengthIncorrect(11)) return false;
        if (isAnyForbiddenCharacters()) return false;
        setIdNumberData();
        if (isCenturyIncorrect()) return false;
        fixYear();
        setBirthDate();
        if (birthDate == null) return false;
        if (isBirthDateInFuture()) return false;
        return checkControlNumber();
    }

    protected boolean isLengthIncorrect(int validLength) {
        return getIdNumber().length() != validLength;
    }

    protected abstract boolean isAnyForbiddenCharacters();

    protected abstract void setIdNumberData();

    protected boolean isCenturyIncorrect() {
        return false;
    }

    protected abstract void fixYear();

    protected void setBirthDate() {
        try {
            this.birthDate = LocalDate.of(idNumberData.year, idNumberData.month, idNumberData.day);
        } catch (DateTimeException e) {
            this.birthDate = null;
        }
    }

    protected boolean isBirthDateInFuture() {
        if (this.birthDate.compareTo(LocalDate.now()) > 0) {
            return true;
        }
        return false;
    }

    protected abstract boolean checkControlNumber();

    public int getAge() {
        Period age = Period.between(birthDate, LocalDate.now());
        return age.getYears();
    }

    public String getIdNumber() {
        return idNumber;
    }

    public IdNumberData getIdNumberData() {
        return idNumberData;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
}


