package de.harich.thilo.factoring.validation;

public class NumberValidator {
    public String validate(String stringToFactorise){
        if (stringToFactorise == null || stringToFactorise.isBlank()){
            return "The field of the number(s) to factorize should not be empty";
        }
        String[] numbers = stringToFactorise.split(",");
        for (String number : numbers){
            String validationIssue = validateNumber(number);
            // only give back the first issue
            if (validationIssue != null)
                return validationIssue;
        }
        return null;
    }

    private String validateNumber(String stringToFactorise) {
        try {
            Long.decode(stringToFactorise);
        } catch (NumberFormatException | NullPointerException e) {
            return "The string '" + stringToFactorise + "' does not represent a integer value with at most 19 digits, and such can not be factorized";
        }
        return null;
    }
}
