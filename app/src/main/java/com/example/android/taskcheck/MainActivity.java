package com.example.android.taskcheck;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

	// Data fields
	static List<TaskData> tasks;
	static String dueDate = "none";
	static String dueTime = "none";
	static String taskPriority = "none";
	TasksListAdapater adapter;
	TaskDatabase database;

	/**
	 * First method that gets called when the app is launched. All instantiations and inflations here.
	 */
	@SuppressLint("WrongThread")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// Set main content view
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialise database access variable. Main Thread Queries allowed as app isn't expected to
		// perform very heavy tasks
		database = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_data")
				.allowMainThreadQueries()
				.fallbackToDestructiveMigration()
				.build();

		// Fetched data from SharedPref. Indicates whether app has been launched at least one time or not
		boolean firstAppLaunch = getPreferences(Context.MODE_PRIVATE).getBoolean("display_intro", true);

		// Fetching stored task list from the database
		tasks = database.taskDao().getAll();

		// Deleting existing task list. New one will be saved upon app closure.
		database.clearAllTables();

		// Initialize and bind recyclerView
		adapter = new TasksListAdapater();
		RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);

		// Displays a snackbar with information on how to mark a task as completed (long click)
		// Displayed only at the first launch
		if (firstAppLaunch) {
			displayIntroSnackbar();
		}
	}

	/**
	 * Displays a snackbar to the user when launching the app for the first time or when clicked on
	 * the menu button. The snackbar display info on how to mark a task as completed
	 */
	private void displayIntroSnackbar() {
		final Snackbar snackbar = Snackbar.make(findViewById(R.id.parent_view), R.string.howto_mark_complete, Snackbar.LENGTH_LONG);
		snackbar.setAction("Dismiss", v -> snackbar.dismiss()).show();
	}

	/**
	 * Used for saving the data in the app to SharedPreferences before the app closes.
	 */
	@Override
	protected void onPause() {
		super.onPause();

		// Indicates that the app has been launched for the first time, so no SnackBar from the next time
		getPreferences(Context.MODE_PRIVATE).edit().putBoolean("display_intro", false).apply();

		// Save tasks data to the database
		if (tasks.size() > 0) {
			database.taskDao().insertAll(tasks);
		}
	}

	/**
	 * Adds a task to the current list and notifies recyclerView of dataset change
	 */
	public void addTask(View view) {
		EditText editTextTaskDescription = findViewById(R.id.edit_text_task_description);
		String taskDescription = editTextTaskDescription.getText().toString();

		// If non-empty string is received, add it to the task list and notify adapter
		if (taskDescription.length() != 0) {
			tasks.add(new TaskData(0, taskDescription, dueDate, dueTime, taskPriority));
			editTextTaskDescription.setText("");    // Resets Task Input field

			// Resets the optional task attributes
			dueDate = "none";
			dueTime = "none";
			taskPriority = "none";
			adapter.notifyDataSetChanged();
		} else {
			// Errors out with a toast.
			Toast.makeText(this, "Cannot add empty Task!", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Inflate the AppBar menu
	 *
	 * @param menu menu object
	 * @return boolean value indicating success
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	/**
	 * Handles AppBar menu option clicks
	 *
	 * @param item the clicked menu entry
	 * @return status indicating success
	 */
	@SuppressLint("NonConstantResourceId")
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_action_clear_all: {
				tasks.clear();
				adapter.notifyDataSetChanged();
				Toast.makeText(this, "Task list cleared!", Toast.LENGTH_SHORT).show();
				return true;
			}
			case R.id.redisplay_intro: {
				displayIntroSnackbar();
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Displays DatePicker for due date selection
	 */
	public void showDatePickerDialog(View view) {
		DialogFragment fragment = new DatePickerFragment();
		fragment.show(getSupportFragmentManager(), "datePicker");
	}

	/**
	 * Displays TimePicker for due time selection
	 */
	public void showTimePickerDialog(View view) {
		DialogFragment fragment = new TimePickerFragment();
		fragment.show(getSupportFragmentManager(), "timePicker");
	}

	/**
	 * Instantiates and displays a new Popup menu to set task priority
	 */
	public void showPriorityMenu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
		popup.setOnMenuItemClickListener(this);
		getMenuInflater().inflate(R.menu.priority_menu, popup.getMenu());
		popup.show();
	}

	/**
	 * Instantiates and displays a Popup menu to sort lists by either due time or priority
	 */
	public void showSortByPopupMenu(View view) {
		PopupMenu popupMenu = new PopupMenu(this, view);
		popupMenu.setOnMenuItemClickListener(this);
		getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());
		popupMenu.show();
	}

	/**
	 * Handles Popup menu option click events. Applicable for both sorting and task priority
	 * setter menus
	 *
	 * @return status code indicating whether event has been handled or not
	 */
	@SuppressLint("NonConstantResourceId")
	@Override
	public boolean onMenuItemClick(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.action_priority_urgent:
				taskPriority = "Urgent";
				return true;
			case R.id.action_priority_rushed:
				taskPriority = "Rushed";
				return true;
			case R.id.action_priority_regular:
				taskPriority = "Regular";
				return true;
			case R.id.action_sort_by_date:
				Helper.sortByDate(tasks);
				adapter.notifyDataSetChanged();
				return true;
			case R.id.action_sort_by_priority:
				Helper.sortByPriority(tasks);
				adapter.notifyDataSetChanged();
			default:
				return false;
		}
	}
}