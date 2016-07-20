package org.robotframework.formslibrary.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.accessibility.AccessibleContext;

import org.robotframework.formslibrary.FormsLibraryException;

public class ComponentUtil {

    private static final Pattern GENERATED_NAME_PATTERN = Pattern.compile("[A-Z_]*[0-9]*");

    public static String getFormattedComponentNames(Component component) {

        List<String> names = getComponentNames(component);
        StringBuilder builder = new StringBuilder(names.get(0));
        if (builder.length() == 0) {
            builder.append("${EMPTY}");
        }
        for (int i = 1; i < names.size(); i++) {
            builder.append(" (").append(names.get(i)).append(")");
        }
        return builder.toString();
    }

    /**
     * Check if one of the component names matches the given name.
     */
    public static boolean hasName(Component component, String name) {

        if (name == null) {
            return false;
        }

        for (String componentName : getComponentNames(component)) {
            if (TextUtil.matches(componentName, name)) {
                return true;
            }
        }

        return false;
    }

    private static List<String> getComponentNames(Component component) {

        List<String> componentNames = new ArrayList<String>();
        String componentType = component.getClass().getName();

        if (componentType.equals(ComponentType.PUSH_BUTTON) || componentType.equals(ComponentType.MENU)
                || componentType.equals(ComponentType.EXTENDED_CHECKBOX)) {
            String label = ObjectUtil.getString(component, "getLabel()");
            if (label != null) {
                componentNames.add(label);
            }
        }

        String accesibleText = getAccessibleText(component);
        if (accesibleText != null && !componentNames.contains(accesibleText) && !GENERATED_NAME_PATTERN.matcher(accesibleText).matches()) {
            componentNames.add(accesibleText);
        }

        String tooltip = getToolTipText(component);
        if (tooltip != null && !componentNames.contains(tooltip)) {
            componentNames.add(tooltip);
        }

        String defaultName = component.getName();
        if (componentNames.isEmpty()) {
            // add a blank string so we can select using blank names
            componentNames.add("");
            // add the default name
            componentNames.add(defaultName);
        }

        return componentNames;
    }

    private static String getToolTipText(Component component) {

        String text = null;

        try {
            Object toolTip = ObjectUtil.invoke(component, "getToolTipValue()");
            if (toolTip != null) {
                text = (String) ObjectUtil.invokeWithIntArg(toolTip, "getToolTipProperty()", 409);
            }
        } catch (FormsLibraryException e) {
            return null;
        }

        if (text != null && text.trim().length() == 0) {
            return null;
        }

        return text;
    }

    public static String getAccessibleText(Component component) {

        AccessibleContext c = component.getAccessibleContext();
        if (c != null) {
            String desc = c.getAccessibleDescription();
            if (desc != null) {
                return TextUtil.removeNewline(desc);
            }
        }
        return null;
    }

    public static boolean isEditable(Component component) {

        String componentType = component.getClass().getName();
        boolean editable = false;

        if (componentType.equals(ComponentType.TEXT_FIELD) || componentType.equals(ComponentType.TEXT_AREA)) {
            editable = ObjectUtil.getBoolean(component, "isEditable()");
        }
        return editable;
    }

    /**
     * Simulate a left mouse click on the given component.
     */
    public static void simulateMouseClick(Component component) {

        component.dispatchEvent(new MouseEvent(component, MouseEvent.MOUSE_PRESSED, 0, MouseEvent.BUTTON1, 5, 5, 1, false));
        component.dispatchEvent(new MouseEvent(component, MouseEvent.MOUSE_RELEASED, 0, MouseEvent.BUTTON1, 5, 5, 1, false));
    }

    /**
     * Simulate a key press/release event on the component.
     * 
     * @param key
     *            KeyEvent.VK_...
     */
    public static void simulateKeyPressed(Component component, int key) {
        component.dispatchEvent(new KeyEvent(component, KeyEvent.KEY_PRESSED, 0, 1, key, KeyEvent.CHAR_UNDEFINED));
        component.dispatchEvent(new KeyEvent(component, KeyEvent.KEY_RELEASED, 0, 2, key, KeyEvent.CHAR_UNDEFINED));
    }

    public static boolean containsComponent(Component parent, Component component) {
        if (parent == component) {
            return true;
        } else if (parent instanceof Container) {
            Component[] childComponents = ((Container) parent).getComponents();
            for (Component child : childComponents) {
                if (containsComponent(child, component)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if comp2 is located to the right of comp1.
     */
    public static boolean areAdjacent(Component comp1, Component comp2) {

        if (areaAlignedVertically(comp1, comp2)) {
            int deltaX = comp2.getX() - (comp1.getX() + comp1.getWidth());
            if (-3 < deltaX && deltaX < 15) {
                Logger.info("Found adjacent field " + comp1.getX() + "-" + (comp1.getX() + comp1.getWidth()) + "," + comp1.getY() + " / "
                        + comp2.getY() + "," + comp2.getY() + ".");
                return true;
            }
        }
        Logger.debug("No match " + comp1.getX() + "-" + (comp1.getX() + comp1.getWidth()) + "," + comp1.getY() + " / " + comp2.getY() + ","
                + comp2.getY() + ".");
        return false;
    }

    /**
     * Check if comp1 is on the same vertical level as comp2.
     */
    public static boolean areaAlignedVertically(Component comp1, Component comp2) {
        int deltaY = comp1.getY() - comp2.getY();
        if (-3 < deltaY && deltaY < 3) {
            return true;
        }
        return false;
    }
}
