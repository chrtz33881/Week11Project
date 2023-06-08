package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
	private Scanner scanner = new Scanner(System.in);
	private ProjectService projectService = new ProjectService();
	private Project curProject;

	// List of available operations for the user
	// @formatter:off
	private List<String> operations = List.of(
			"1) Add a project", 
			"2) List projects", 
			"3) Select a project",
			"4) Update project details",
			"5) Delete a project"
		);
	// @formatter:on

	public static void main(String[] args) {
		new ProjectsApp().processUserSelections();
	}

	// Process user selections until done
	private void processUserSelections() {
		boolean done = false;

		while (!done) {
			try {
				int selection = getUserSelection();

				switch (selection) {
				case -1:
					done = exitMenu(); // Exit the menu if -1 is selected
					break;

				case 1:
					createProject(); // Create a new project if 1 is selected
					break;

				case 2:
					listProjects(); // Call the method to list all projects
					break;

				case 3:
					selectProject(); // Call the method to select a project
					break;

				case 4:
					updateProjectDetails(); // Call the method to update project details
					break;

				case 5:
					deleteProject(); // Call the method to delete Projects
					break;

				default:
					System.out.println("\n" + selection + " is not a valid selection. Try again.");
				}
			} catch (Exception e) {
				System.out.println("\nError: " + e + " Try again.");
			}
		}
	}

	private void updateProjectDetails() {
		if (Objects.isNull(curProject)) {
			// Check if there is a currently selected project
			System.out.println("\nPlease select a project.");
			return;
		}

		// Prompt the user to update project details
		String projectName = getStringInput("Enter the project name [" + curProject.getProjectName() + "]");
		BigDecimal estimatedHours = getDecimalInput(
				"Enter the estimated hours [" + curProject.getEstimatedHours() + "]");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours + [" + curProject.getActualHours() + "]");
		Integer difficulty = getIntInput("Enter the project difficulty (1-5) [" + curProject.getDifficulty() + "]");
		String notes = getStringInput("Enter the project notes [" + curProject.getNotes() + "]");

		// Create a new Project object to hold the updated details
		Project project = new Project();

		// Set the project ID
		project.setProjectId(curProject.getProjectId());

		// Update the project name if entered, otherwise keep the current name
		project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);

		// Update the estimated hours if entered, otherwise keep the current value
		project.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);

		// Update the actual hours if entered, otherwise keep the current value
		project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);

		// Update the difficulty level if entered, otherwise keep the current value
		project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);

		// Update the project notes if entered, otherwise keep the current notes
		project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);

		// Call the projectService to modify the project details
		projectService.modifyProjectDetails(project);

		// Fetch the updated project details from the projectService and assign it to
		// curProject
		curProject = projectService.fetchProjectById(curProject.getProjectId());
	}

	private void deleteProject() {
		listProjects();

		// Prompt the user to enter the ID of the project to delete
		Integer projectId = getIntInput("Enter the ID of the project to delete");

		// Call the projectService to delete the project with the specified ID
		projectService.deleteProject(projectId);

		// Display a success message if the project was deleted
		System.out.println("Project " + projectId + " was deleted successfully.");

		// Check if the currently selected project is being deleted and set curProject
		// to null if so
		if (Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
			curProject = null;
		}
	}

	private void selectProject() {
		listProjects(); // Call the method to list all projects

		Integer projectId = getIntInput("Enter a project ID to select a project"); // Get the project ID from the user

		curProject = null; // Reset the current project

		curProject = projectService.fetchProjectById(projectId); // Fetch the selected project using the project ID
	}

	// Method to list all projects
	private void listProjects() {
		List<Project> projects = projectService.fetchAllProjects(); // Retrieve all projects from the project service

		System.out.println("\nProjects:"); // Print a heading for the list of projects

		// Iterate over the projects and print their ID and name
		projects.forEach(
				project -> System.out.println("   " + project.getProjectId() + ": " + project.getProjectName()));
	}

	// Get user selection from available operations
	private int getUserSelection() {
		printOperations(); // Print available operations to the user
		Integer input = getIntInput("Enter a menu selection"); // Get integer input from the user

		return Objects.isNull(input) ? -1 : input;
	}

	// Print available operations to the user
	private void printOperations() {
		System.out.println("\nThese are the available selections. Press the Enter key to quit:");

		operations.forEach(line -> System.out.println("  " + line));

		if (Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project.");
		} else {
			System.out.println("\nYou are working with project: " + curProject);
		}
	}

	// Get integer input from the user
	private Integer getIntInput(String prompt) {
		String input = getStringInput(prompt);

		if (Objects.isNull(input)) {
			return null;
		}
		try {
			return Integer.valueOf(input);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid number.");
		}
	}

	// Get string input from the user
	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String input = scanner.nextLine();

		return input.isBlank() ? null : input.trim();
	}

	// Exit the menu
	private boolean exitMenu() {
		System.out.println("Exiting the menu...");
		return true;
	}

	// Create a new project
	private void createProject() {
		String projectName = getStringInput("Enter the project name");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours");
		Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
		String notes = getStringInput("Enter the project notes");

		// Create a new Project object and set its properties
		Project project = new Project();
		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);

		// Add the project to the database through the project service
		Project dbProject = projectService.addProject(project);
		System.out.println("You have successfully created project: " + dbProject);
	}

	// Get decimal input from the user
	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);
		if (Objects.isNull(input)) {
			return null;
		}
		try {
			return new BigDecimal(input).setScale(2);
		} catch (NumberFormatException e) {
			throw new DbException(input + " is not a valid decimal number.");
		}
	}
}