package idNumber;

public class EEIdNumber extends IdNumber {

    public EEIdNumber(String idNumber) {
        super(idNumber);
    }

    protected boolean isAnyForbiddenCharacters() {
        char[] idNumberChars = getIdNumber().toCharArray();
        for (char idNumberChar : idNumberChars) {
            if (!Character.isDigit(idNumberChar)) {
                return true;
            }
        }
        return false;
    }

    protected void setIdNumberData() {
        StringBuilder stringBuilder = new StringBuilder(getIdNumber());
        this.idNumberData.century = Integer.parseInt(stringBuilder.substring(0, 1));
        this.idNumberData.year = Integer.parseInt(stringBuilder.substring(1, 3));
        this.idNumberData.month = Integer.parseInt(stringBuilder.substring(3, 5));
        this.idNumberData.day = Integer.parseInt(stringBuilder.substring(5, 7));
        this.idNumberData.serialNumber = Integer.parseInt(stringBuilder.substring(7, 10));
        this.idNumberData.controlNumber = Integer.parseInt(stringBuilder.substring(10, 11));
    }

    @Override
    protected boolean isCenturyIncorrect() {
        if (idNumberData.century < 1 || idNumberData.century > 6) {
            return true;
        }
        return false;
    }

    protected void fixYear() {
        switch (idNumberData.century) {
            case 1, 2 -> idNumberData.year += 1800;
            case 3, 4 -> idNumberData.year += 1900;
            case 5, 6 -> idNumberData.year += 2000;
        }
    }

    protected boolean checkControlNumber() {
        char[] idNumberChars = getIdNumber().toCharArray();
        int[] controlNumberI = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1};
        int[] controlNumberII = new int[]{3, 4, 5, 6, 7, 8, 9, 1, 2, 3};
        int controlNumberSum = 0;
        int controlNumber;
        for (int i = 0; i < idNumberChars.length - 1; i++) {
            controlNumberSum += (idNumberChars[i] - 48) * controlNumberI[i];
        }
        if (controlNumberSum % 11 < 10) {
            controlNumber = controlNumberSum % 11;
        } else {
            controlNumberSum = 0;
            for (int i = 0; i < idNumberChars.length-1; i++) {
                controlNumberSum += idNumberChars[i] * controlNumberII[i];
            }
            if (controlNumberSum % 11 < 10) {
                controlNumber = controlNumberSum % 11;
            } else {
                controlNumber = 0;
            }
        }
        return controlNumber == idNumberData.controlNumber;
    }
}
