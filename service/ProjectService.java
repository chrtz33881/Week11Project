package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
    private ProjectDao projectDao = new ProjectDao(); // Create an instance of ProjectDao

    // Add a project by calling the insertProject method of ProjectDao
    public Project addProject(Project project) {
        return projectDao.insertProject(project);
    }
        
 // Method to fetch all projects
    public List<Project> fetchAllProjects() {
        return projectDao.fetchAllProjects(); // Delegates the task to the projectDao to fetch all projects
    }

    // Method to fetch a project by its ID
    public Project fetchProjectById(Integer projectId) {
        return projectDao.fetchProjectById(projectId)
            .orElseThrow(() -> new NoSuchElementException(
                "Project with project ID=" + projectId + " does not exist.")); // Delegates the task to the projectDao to fetch a project by its ID,
                                                                             // and throws a NoSuchElementException if the project does not exist
    }

    public void modifyProjectDetails(Project project) {
    	// Call the modifyProjectDetails() method in the projectDao to update the project details
    	if (!projectDao.modifyProjectDetails(project)) {
    		// If the modifyProjectDetails() method returns false, indicating the project does not exist, throw a DbException with an appropriate error message
    		throw new DbException("Project with ID=" + project.getProjectId() + " does not exist.");
    	}
    }

    public void deleteProject(Integer projectId) {
    	// Call the deleteProject() method in the projectDao to delete the project with the specified ID
    	if (!projectDao.deleteProject(projectId)) {
    		// If the deleteProject() method returns false, indicating the project does not exist, throw a DbException with an appropriate error message
    		throw new DbException("Project with ID=" + projectId + " does not exist.");
    	}
    }

}


