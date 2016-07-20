package org.robotframework.formslibrary.operator;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.jemmy.ComponentChooser;
import org.robotframework.formslibrary.chooser.ByClassChooser;
import org.robotframework.formslibrary.chooser.ByNameChooser;
import org.robotframework.formslibrary.context.FormsContext;
import org.robotframework.formslibrary.util.ComponentType;
import org.robotframework.formslibrary.util.ComponentUtil;

public class ContextOperator {

    public Container getSource() {
        return (Container) FormsContext.getContext().getSource();
    }

    /**
     * Finds all visible components matching the chooser in the current context.
     */
    public List<Component> findComponents(ComponentChooser chooser) {
        return findChildComponentsByChooser(getSource(), chooser);
    }

    /**
     * Find all childComponents that match a given chooser selection. Components
     * which are not visible are ignored.
     */
    private List<Component> findChildComponentsByChooser(Component component, ComponentChooser chooser) {

        List<Component> result = new ArrayList<Component>();

        if (chooser.checkComponent(component)) {
            if (component.isShowing()) {
                // don't include components that are not visible in the UI
                result.add(component);
            }
        } else if (component instanceof Container) {
            Component[] childComponents = ((Container) component).getComponents();
            for (Component child : childComponents) {
                result.addAll(findChildComponentsByChooser(child, chooser));
            }
        }

        return result;
    }

    public void listComponents(String... componentTypes) {
        for (Component component : findComponents(new ByClassChooser(-1, componentTypes))) {

            String editable = "";
            if (ComponentUtil.isEditable(component)) {
                editable = " [editable] ";
            }

            String location = String.format("%1$-8s", component.getX() + "," + component.getY());
            System.out.println(location + " : " + ComponentUtil.getFormattedComponentNames(component) + editable);
        }
    }

    public void listTextFields(String... componentTypes) {
        for (Component component : findComponents(new ByClassChooser(-1, ComponentType.ALL_TEXTFIELD_TYPES))) {

            String editable = "";
            if (ComponentUtil.isEditable(component)) {
                editable = " [editable] ";
            }

            TextFieldOperator operator = TextFieldOperatorFactory.getOperator(component);
            String value = " : " + operator.getValue();

            String location = String.format("%1$-8s", component.getX() + "," + component.getY());
            System.out.println(location + " : " + ComponentUtil.getFormattedComponentNames(component) + value + editable);
        }
    }

    /**
     * Print a full hierarchy of all components in the current context.
     */
    public void listComponentHierarchy() {
        printHierarchyLevel(getSource(), 0);
    }

    private void printHierarchyLevel(Component component, int level) {

        String editable = "";
        if (ComponentUtil.isEditable(component)) {
            editable = " [editable] ";
        }
        String formattedName = String.format("%1$-" + (2 * (level + 1)) + "s", "L" + level) + component.getClass().getName() + "  -  "
                + ComponentUtil.getFormattedComponentNames(component) + editable;
        System.out.println(formattedName);

        if (component instanceof Container) {
            Component[] childComponents = ((Container) component).getComponents();
            level++;
            for (Component child : childComponents) {
                printHierarchyLevel(child, level);
            }
            level--;
        }

    }

    private List<Component> findNonTableTextFields() {
        return purgeTableFields(findComponents(new ByClassChooser(-1, ComponentType.ALL_TEXTFIELD_TYPES)));
    }

    /**
     * Remove all fields from the list which are organized in a table layout
     * (same name + same X coordinates)
     */
    private List<Component> purgeTableFields(List<Component> components) {
        List<Component> result = new ArrayList<Component>();
        for (Component component : components) {
            boolean isTableCell = false;

            for (Component otherComponent : components) {
                if (component == otherComponent) {
                    continue;
                }
                if (component.getX() == otherComponent.getX()) {

                    // only take other fields that are really close into account
                    int yDelta = component.getY() - otherComponent.getY();
                    if (-35 < yDelta && yDelta < 35) {
                        String name = "" + ComponentUtil.getAccessibleText(component);
                        if (("" + ComponentUtil.getAccessibleText(otherComponent)).equals(name)) {
                            isTableCell = true;
                            break;
                        }
                    }
                }
            }
            if (!isTableCell) {
                result.add(component);
            }
        }
        return result;
    }

    public Component findTextField(ByNameChooser chooser) {

        for (Component component : findNonTableTextFields()) {
            if (chooser.checkComponent(component)) {
                return component;
            }
        }

        return null;
    }

}
