package org.robotframework.formslibrary.operator;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.jemmy.ComponentChooser;
import org.robotframework.formslibrary.chooser.ByComponentTypeChooser;
import org.robotframework.formslibrary.chooser.ByNameChooser;
import org.robotframework.formslibrary.context.FormsContext;
import org.robotframework.formslibrary.util.ComponentComparator;
import org.robotframework.formslibrary.util.ComponentType;
import org.robotframework.formslibrary.util.ComponentUtil;
import org.robotframework.formslibrary.util.Logger;
import org.robotframework.swing.operator.ComponentWrapper;

/**
 * Operator for the current or given context. This operator allows searching for
 * components in the context without resulting in an error if nothing is found.
 */
public class ContextOperator {

	private Container context;

	/**
	 * Initialize a context operator for the current context.
	 */
	public ContextOperator() {
		this(FormsContext.getContext());
	}

	/**
	 * Initialize a context operator for the given context.
	 */
	public ContextOperator(ComponentWrapper contextWrapper) {
		this.context = (Container) contextWrapper.getSource();
	}

	/**
	 * @return Component representing the context.
	 */
	public Container getSource() {
		return context;
	}

	/**
	 * Finds all visible components matching the chooser in the context.
	 */
	public List<Component> findComponents(ComponentChooser chooser) {
		return findChildComponentsByChooser(getSource(), chooser);
	}

	/**
	 * Finds all visible components matching the chooser in the context.
	 */
	private List<Component> findAndSortComponents(ComponentChooser chooser) {
		List<Component> results = findChildComponentsByChooser(getSource(), chooser);
		Collections.sort(results, new ComponentComparator());
		return results;
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

	/**
	 * Print a list of all components in the current context, which match the
	 * given types.
	 */
	public void listComponents(ComponentType... componentTypes) {
		for (Component component : findAndSortComponents(new ByComponentTypeChooser(-1, componentTypes))) {

			String editable = "";
			if (ComponentUtil.isEditable(component)) {
				editable = " [editable] ";
			}
			Logger.info(getFormattedLocation(component) + " : " + ComponentUtil.getFormattedComponentNames(component) + editable);
		}
	}

	private String getFormattedLocation(Component component) {
		Point location = ComponentUtil.getLocationInWindow(component);
		return String.format("%1$-8s", location.x + "," + location.y);
	}

	/**
	 * Print all the text fields in the current context.
	 */
	public void listTextFields() {
		for (Component component : findAndSortComponents(new ByComponentTypeChooser(-1, ComponentType.ALL_TEXTFIELD_TYPES))) {

			String editable = "";
			if (ComponentUtil.isEditable(component)) {
				editable = " [editable] ";
			}

			TextFieldOperator operator = TextFieldOperatorFactory.getOperator(component);
			String value = " : " + operator.getValue();
			Logger.info(getFormattedLocation(component) + " : " + ComponentUtil.getFormattedComponentNames(component) + value + editable);
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
		String formattedName = String.format("%1$-" + (10 + (2 * (level + 1))) + "s", "L" + level + " [" + getFormattedLocation(component) + "]")
				+ component.getClass().getName() + "  -  " + ComponentUtil.getFormattedComponentNames(component) + editable
				+ ComponentUtil.getValue(component);
		Logger.info(formattedName);

		if (component instanceof Container) {
			Component[] childComponents = ((Container) component).getComponents();
			level++;
			for (Component child : childComponents) {
				printHierarchyLevel(child, level);
			}
			level--;
		}

	}

	public List<Component> findNonTableTextFields() {
		return purgeTableFields(findComponents(new ByComponentTypeChooser(-1, ComponentType.ALL_TEXTFIELD_TYPES)));
	}

	private List<Component> findTableTextFields() {
		List<Component> result = findComponents(new ByComponentTypeChooser(-1, ComponentType.ALL_TEXTFIELD_TYPES));
		List<Component> nonTableTextFieds = findNonTableTextFields();
		result.removeAll(nonTableTextFieds);
		Collections.sort(result, new ComponentComparator());
		return result;
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

				Point loc1 = ComponentUtil.getLocationInWindow(component);
				int height = component.getHeight();
				Point loc2 = ComponentUtil.getLocationInWindow(otherComponent);

				if (loc1.x == loc2.x) {

					// only take other fields that are really close into account
					int yDelta = loc1.y - loc2.y;

					if (Math.abs(yDelta) - height < 2) {
						String compName = "" + ComponentUtil.getAccessibleText(component);
						String otherCompName = "" + ComponentUtil.getAccessibleText(otherComponent);
						if (!"null".equals(compName) && otherCompName.equals(compName)) {
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

	/**
	 * Find a specific text field by name in the current context. Text fields
	 * which are arranged in a table layout are ignored.
	 */
	public Component findTextField(ByNameChooser chooser) {

		for (Component component : findNonTableTextFields()) {
			if (chooser.checkComponent(component)) {
				return component;
			}
		}

		return null;
	}

	/**
	 * Find a table text field by name in the current context. Only fields in a
	 * table layout are included.
	 */
	public List<Component> findTableFields(ByNameChooser chooser) {

		List<Component> results = new ArrayList<Component>();

		for (Component component : findTableTextFields()) {
			if (chooser.checkComponent(component)) {
				results.add(component);
			}
		}

		return results;
	}

	/**
	 * Capture a screenshot of the current context.
	 */
	public String capture(String targetDirectory) {
		return ComponentUtil.captureToFile(targetDirectory, getSource());
	}

	public void initMissingComponentNames() {
		initMissingComponentNames(getSource());
	}

	private void initMissingComponentNames(Component component) {
		// Start witn empty counters
		nameCounters = new HashMap<String, Integer>();
		doInitMissingComponentNames(component);
	}

	private void doInitMissingComponentNames(Component component) {
		String name = component.getName();
		if (name == null) {
			generateName(component);
		}
		if (component instanceof Container) {
			Component[] childComponents = ((Container) component).getComponents();
			for (Component child : childComponents) {
				doInitMissingComponentNames(child);
			}
		}
	}

	private static Map<String, Integer> nameCounters = new HashMap<String, Integer>();

	private void generateName(Component component) {
		String className = getBaseClassName(component.getClass());
		Integer count = nameCounters.get(className);
		if (count == null) {
			count = 0;
		}
		count++;
		nameCounters.put(className, count);
		component.setName("_" + className + count.intValue());

	}

	protected final String getBaseClassName(Class<?> clazz) {
		String str = clazz.getName();
		return str.substring(str.lastIndexOf('.') + 1);
	}

}
