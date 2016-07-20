package org.robotframework.formslibrary.keyword;

import org.robotframework.formslibrary.operator.TextFieldOperatorFactory;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

@RobotKeywords
public class TextFieldKeywords {

    @RobotKeyword("Locate a field by name and set it to the given value. ':' in the field labels are ignored.\n\n" + "Example:\n"
            + "| Set Field | _username_ | _jeff_ | \n")
    @ArgumentNames({ "identifier", "value" })
    public void setField(String identifier, String value) {
        TextFieldOperatorFactory.getOperator(identifier).setValue(value);
    }

    @RobotKeyword("Verify field content.\n\n" + "Example:\n" + "| Field Should Contain | _username_ | _jeff_ | \n")
    @ArgumentNames({ "identifier", "value" })
    public void fieldShouldContain(String identifier, String value) {
        TextFieldOperatorFactory.getOperator(identifier).verifyValue(value);
    }

    @RobotKeyword("Get field content.\n\n" + "Example:\n" + "| \r\n" + "| ${textFieldValue}= | Get Field | _username_ | \n")
    @ArgumentNames({ "identifier" })
    public String getField(String identifier) {
        return TextFieldOperatorFactory.getOperator(identifier).getValue();
    }

}
