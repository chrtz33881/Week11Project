package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

@SuppressWarnings("unused")
public class ProjectDao extends DaoBase {
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	// Insert a project into the database
	public Project insertProject(Project project) {
		String sql = "INSERT INTO " + PROJECT_TABLE
				+ " (project_name, estimated_hours, actual_hours, difficulty, notes) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn); // Start a transaction

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				// Set the parameter values for the SQL statement
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);

				stmt.executeUpdate(); // Execute the SQL statement

				// Get the last inserted project's ID from the database
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);

				commitTransaction(conn); // Commit the transaction

				project.setProjectId(projectId); // Set the project's ID
				return project; // Return the project
			} catch (Exception e) {
				throw new DbException(e); // Throw a custom exception if an error occurs during statement execution
			}
		} catch (SQLException e) {
			throw new DbException(e); // Throw a custom exception if a SQL error occurs
		}
	}

	// Method to fetch all projects from the database
	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn); // Start a transaction

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				try (ResultSet rs = stmt.executeQuery()) {
					List<Project> projects = new LinkedList<>();

					while (rs.next()) {
						projects.add(extract(rs, Project.class)); // Extract project data from the result set and add it
																	// to the list
					}

					return projects; // Return the list of projects
				}
			} catch (Exception e) {
				rollbackTransaction(conn); // Rollback the transaction if an exception occurs
				throw new DbException(e); // Throw a custom exception indicating a database error
			}
		} catch (SQLException e) {
			throw new DbException(e); // Throw a custom exception indicating a database error
		}
	}

	// Method to fetch a project by its ID from the database
	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn); // Start a transaction

			try {
				Project project = null;

				try (PreparedStatement stmt = conn.prepareStatement(sql)) {
					setParameter(stmt, 1, projectId, Integer.class); // Set the project ID parameter

					try (ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							project = extract(rs, Project.class); // Extract project data from the result set
						}
					}
				}

				if (Objects.nonNull(project)) {
					// Fetch additional data related to the project
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}

				commitTransaction(conn); // Commit the transaction
				return Optional.ofNullable(project); // Return the project wrapped in an Optional
			} catch (Exception e) {
				rollbackTransaction(conn); // Rollback the transaction if an exception occurs
				throw new DbException(e); // Throw a custom exception indicating a database error
			}
		} catch (SQLException e) {
			throw new DbException(e); // Throw a custom exception indicating a database error
		}
	}

	// Method to fetch categories for a project
	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		// SQL query to fetch categories for a project
		String sql = "SELECT c.* FROM " + CATEGORY_TABLE + " c " + "JOIN " + PROJECT_CATEGORY_TABLE
				+ " pc USING (category_id) " + "WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class); // Set the project ID parameter

			try (ResultSet rs = stmt.executeQuery()) {
				List<Category> categories = new LinkedList<>();

				while (rs.next()) {
					categories.add(extract(rs, Category.class)); // Extract category data from the result set and add it
																	// to the list
				}

				return categories; // Return the list of categories
			}
		}
	}

	// Method to fetch steps for a project
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class); // Set the project ID parameter

			try (ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<>();

				while (rs.next()) {
					steps.add(extract(rs, Step.class)); // Extract step data from the result set and add it to the list
				}

				return steps; // Return the list of steps
			}
		}
	}

	// Method to fetch materials for a project
	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class); // Set the project ID parameter

			try (ResultSet rs = stmt.executeQuery()) {
				List<Material> materials = new LinkedList<>();

				while (rs.next()) {
					materials.add(extract(rs, Material.class)); // Extract material data from the result set and add it
																// to the list
				}

				return materials; // Return the list of materials
			}
		}
	}

	public boolean modifyProjectDetails(Project project) {
		// SQL query to update project details in the database
		// @formatter:off
		String sql = "" 
				+ "UPDATE " 
				+ PROJECT_TABLE 
				+ " SET " 
				+ "project_name = ?, " 
				+ "estimated_hours = ?, "
				+ "actual_hours = ?, " 
				+ "difficulty = ?, " 
				+ "notes = ? " 
				+ "WHERE project_id = ?";
		// @formatter:on

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn); // Start a transaction for database operations

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				// Set the parameters in the SQL statement using the project object's properties
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);

				// Execute the SQL statement and check if exactly one row was modified
				boolean modified = stmt.executeUpdate() == 1;
				commitTransaction(conn); // Commit the transaction

				return modified; // Return whether the project details were modified or not
			} catch (Exception e) {
				rollbackTransaction(conn); // Rollback the transaction in case of an exception
				throw new DbException(e); // Throw a DbException with the caught exception
			}
		} catch (SQLException e) {
			throw new DbException(e); // Throw a DbException with the caught SQLException
		}
	}

	public boolean deleteProject(Integer projectId) {
		// SQL query to delete a project from the database based on project_id
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn); // Start a transaction for database operations

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class); // Set the project_id parameter in the SQL statement

				// Execute the SQL statement and check if exactly one row was deleted
				boolean deleted = stmt.executeUpdate() == 1;

				commitTransaction(conn); // Commit the transaction
				return deleted; // Return whether the project was deleted or not
			} catch (Exception e) {
				rollbackTransaction(conn); // Rollback the transaction in case of an exception
				throw new DbException(e); // Throw a DbException with the caught exception
			}
		} catch (SQLException e) {
			throw new DbException(e); // Throw a DbException with the caught SQLException
		}
	}
}