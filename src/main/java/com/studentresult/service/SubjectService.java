package com.studentresult.service;

import com.studentresult.dao.SubjectDao;
import com.studentresult.model.Subject;
import com.studentresult.util.ValidationUtil;
import java.sql.SQLException;
import java.util.List;

/**
 * Service managing subjects, semester curriculum configurations, and validation.
 */
public class SubjectService {

    private final SubjectDao subjectDao;

    public SubjectService() {
        this.subjectDao = new SubjectDao();
    }

    public SubjectService(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    /**
     * Adds a new subject to the curriculum.
     */
    public void addSubject(Subject subject) throws IllegalArgumentException, SQLException {
        validateSubject(subject, 0);
        boolean success = subjectDao.insert(subject);
        if (!success) {
            throw new SQLException("Failed to save subject to database.");
        }
    }

    /**
     * Updates an existing subject.
     */
    public void updateSubject(Subject subject) throws IllegalArgumentException, SQLException {
        if (subject.getId() <= 0) {
            throw new IllegalArgumentException("Subject ID must be greater than zero for update.");
        }
        validateSubject(subject, subject.getId());
        boolean success = subjectDao.update(subject);
        if (!success) {
            throw new SQLException("Failed to update subject. Subject not found or no changes made.");
        }
    }

    /**
     * Deletes a subject by ID.
     */
    public void deleteSubject(int subjectId) throws SQLException {
        boolean success = subjectDao.delete(subjectId);
        if (!success) {
            throw new SQLException("Failed to delete subject. Subject not found.");
        }
    }

    /**
     * Retrieves all subjects.
     */
    public List<Subject> getAllSubjects() throws SQLException {
        return subjectDao.findAll();
    }

    /**
     * Retrieves subjects configured for a semester.
     */
    public List<Subject> getSubjectsBySemester(int semester) throws SQLException {
        return subjectDao.findBySemester(semester);
    }

    /**
     * Finds subject by ID.
     */
    public Subject getSubjectById(int id) throws SQLException {
        return subjectDao.findById(id);
    }

    /**
     * Returns total subject count.
     */
    public int getTotalSubjectCount() throws SQLException {
        return subjectDao.countTotalSubjects();
    }

    /**
     * Resets curriculum subjects to default sample configuration.
     */
    public void resetToDefaultSubjects() throws SQLException {
        subjectDao.resetToDefaults();
    }

    /**
     * Validates subject configuration rules.
     */
    public void validateSubject(Subject subject, int excludeId) throws IllegalArgumentException, SQLException {
        if (subject == null) {
            throw new IllegalArgumentException("Subject information cannot be null.");
        }

        if (ValidationUtil.isNullOrBlank(subject.getSubjectCode())) {
            throw new IllegalArgumentException("Subject code is required (e.g. CS101).");
        }

        if (ValidationUtil.isNullOrBlank(subject.getSubjectName())) {
            throw new IllegalArgumentException("Subject name is required.");
        }

        if (!ValidationUtil.isValidSemester(subject.getSemester())) {
            throw new IllegalArgumentException("Semester must be between 1 and 8.");
        }

        if (subject.getMaxMarks() <= 0) {
            throw new IllegalArgumentException("Maximum marks must be greater than 0.");
        }

        if (subject.getPassMarks() < 0 || subject.getPassMarks() > subject.getMaxMarks()) {
            throw new IllegalArgumentException("Passing marks must be between 0 and maximum marks (" + subject.getMaxMarks() + ").");
        }

        if (subject.getCredits() < 0 || subject.getCredits() > 10) {
            throw new IllegalArgumentException("Credits must be between 0 and 10.");
        }

        // Check unique subject code
        if (subjectDao.existsByCode(subject.getSubjectCode(), excludeId)) {
            throw new IllegalArgumentException("A subject with Code '" + subject.getSubjectCode().trim().toUpperCase() + "' already exists.");
        }
    }
}

