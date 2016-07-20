package org.robotframework.formslibrary.operator;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.robotframework.formslibrary.FormsLibraryException;
import org.robotframework.formslibrary.chooser.ByClassChooser;
import org.robotframework.formslibrary.chooser.ByRowChooser;
import org.robotframework.formslibrary.util.ComponentComparator;
import org.robotframework.formslibrary.util.ComponentType;
import org.robotframework.formslibrary.util.ComponentUtil;
import org.robotframework.formslibrary.util.Logger;
import org.robotframework.formslibrary.util.ObjectUtil;
import org.robotframework.formslibrary.util.TextUtil;

public class TableOperator extends ContextOperator {

    /**
     * Locate a matching row by field values.
     * 
     * @return the first field of the matching row.
     */
    private Component findRow(String[] columnValues) {

        Logger.info("Locating row " + TextUtil.formatArray(columnValues));

        List<List<Component>> potentialColumnFieldMatches = new ArrayList<List<Component>>();

        for (String keyValue : columnValues) {
            List<Component> matches = findTextFieldsByValue(keyValue);
            potentialColumnFieldMatches.add(matches);
            Logger.debug("Found " + matches.size() + " potential matches for '" + keyValue + "'.");
        }

        List<Component> keyColumns = potentialColumnFieldMatches.get(0);
        if (keyColumns.isEmpty()) {
            throw new FormsLibraryException("No column found with value '" + columnValues[0] + "'");
        }

        // filter out all columns that don't have an adjacent column
        for (int i = potentialColumnFieldMatches.size(); i > 1; i--) {

            List<Component> rightColumns = potentialColumnFieldMatches.get(i - 1);
            List<Component> leftColumns = potentialColumnFieldMatches.get(i - 2);
            List<Component> toRemove = new ArrayList<Component>();

            for (Component col : leftColumns) {
                if (!hasAdjacentColumn(col, rightColumns)) {
                    toRemove.add(col);
                }
            }
            leftColumns.removeAll(toRemove);

        }

        if (keyColumns.size() == 0) {
            throw new FormsLibraryException("No matching row found.");
        } else if (keyColumns.size() > 1) {
            Logger.info("Multiple rows found. Selecting first one.");
        }

        Component firstField = keyColumns.get(0);
        Logger.info("Found matching row @ " + firstField.getX() + ", " + firstField.getY() + ".");
        return firstField;

    }

    /**
     * Select a row by simulating a mouse click in the first field.
     */
    public void selectRow(String[] columnValues) {
        Component firstRowField = findRow(columnValues);
        ComponentUtil.simulateMouseClick(firstRowField);
    }

    /**
     * Find all checkboxes located on the same vertical position as the table
     * text fields.
     */
    private List<Component> findRowCheckBoxes(Component keyField) {

        List<Component> result = findComponents(new ByClassChooser(-1, ComponentType.CHECK_BOX));
        List<Component> rowCheckboxes = new ArrayList<Component>();

        for (Component box : result) {
            if (ComponentUtil.areaAlignedVertically(keyField, box)) {
                rowCheckboxes.add(box);
            }
        }

        Collections.sort(rowCheckboxes, new ComponentComparator());
        return rowCheckboxes;
    }

    private List<Component> findTextFieldsByValue(String value) {

        List<Component> allTextFields = findComponents(new ByClassChooser(-1, ComponentType.ALL_TEXTFIELD_TYPES));
        List<Component> result = new ArrayList<Component>();

        for (Component textField : allTextFields) {
            TextFieldOperator operator = TextFieldOperatorFactory.getOperator(textField);
            String text = operator.getValue();
            if (TextUtil.matches(text, value)) {
                result.add(textField);
            }
        }

        return result;
    }

    private boolean hasAdjacentColumn(Component firstColumn, List<Component> otherColumns) {

        for (Component nextCol : otherColumns) {
            if (ComponentUtil.areAdjacent(firstColumn, nextCol)) {
                return true;
            }
        }
        return false;
    }

    private CheckboxOperator getCheckboxOperator(int index, String[] columnValues) {

        List<Component> boxes = findRowCheckBoxes(findRow(columnValues));

        if (boxes.size() < index) {
            throw new FormsLibraryException("Only found " + boxes.size() + " checkboxes next to the row");
        }

        return new CheckboxOperator((Component) ObjectUtil.invoke(boxes.get(index - 1), "getLWCheckBox()"));
    }

    public void selectRowCheckbox(int index, String[] columnValues) {
        getCheckboxOperator(index, columnValues).check();
    }

    public void deselectRowCheckbox(int index, String[] columnValues) {
        getCheckboxOperator(index, columnValues).uncheck();
    }

    public boolean getRowCheckboxState(int index, String[] columnValues) {
        return getCheckboxOperator(index, columnValues).isChecked();
    }

    public void setRowField(String identifier, String value, String[] columnValues) {

        Component firstColumn = findRow(columnValues);
        List<Component> results = findComponents(new ByRowChooser(firstColumn, identifier, ComponentType.ALL_TEXTFIELD_TYPES));

        if (results.isEmpty()) {
            throw new FormsLibraryException("No row field found with name '" + identifier + "'");
        }

        TextFieldOperator operator = TextFieldOperatorFactory.getOperator(results.get(0));
        operator.setValue(value);
    }

}
