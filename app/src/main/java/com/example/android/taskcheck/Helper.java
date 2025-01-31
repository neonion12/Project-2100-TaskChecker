package com.example.android.taskcheck;

import java.util.Collections;
import java.util.List;

public class Helper {
	/**
	 * Reverses String containing date
	 *
	 * @param originalString in this format: "YYYY/MM/DD"
	 * @return reversedString in this format: "DD/MM/YYYY"
	 */
	public static String reverseDateString(final String originalString) {
		String[] dateFields = originalString.split("/");
		return dateFields[2] + "/" + dateFields[1] + "/" + dateFields[0];
	}

	/**
	 * Sorts tasks (list) by due date
	 *
	 * @param tasks the list to be sorted.
	 */
	public static void sortByDate(final List<TaskData> tasks) {
		Collections.sort(tasks, (taskData, t1) -> taskData.dueDate.compareTo(t1.toString()));
	}

	/**
	 * Sorts tasks (list) by priority or urgency
	 *
	 * @param tasks the list to be sorted
	 */
	public static void sortByPriority(final List<TaskData> tasks) {
		Collections.sort(tasks, ((taskData, t1) -> t1.taskPriority.compareToIgnoreCase(taskData.taskPriority)));
	}
}
