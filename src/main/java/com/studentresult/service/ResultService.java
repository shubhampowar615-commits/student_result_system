package com.studentresult.service;

import com.studentresult.dao.MarkDao;
import com.studentresult.dao.ResultDao;
import com.studentresult.dao.StudentDao;
import com.studentresult.dao.SubjectDao;
import com.studentresult.model.MarkEntry;
import com.studentresult.model.Result;
import com.studentresult.model.ResultSummary;
import com.studentresult.model.Student;
import com.studentresult.model.Subject;
import com.studentresult.util.ValidationUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service orchestrating marks entry, calculation evaluation, result persistence,
 * and summary reports.
 */
public class ResultService {

    private final MarkDao markDao;
    private final ResultDao resultDao;
    private final StudentDao studentDao;
    private final SubjectDao subjectDao;
    private final CalculationService calculationService;

    public ResultService() {
        this.markDao = new MarkDao();
        this.resultDao = new ResultDao();
        this.studentDao = new StudentDao();
        this.subjectDao = new SubjectDao();
        this.calculationService = new CalculationService();
    }

    public ResultService(MarkDao markDao, ResultDao resultDao, StudentDao studentDao, 
                         SubjectDao subjectDao, CalculationService calculationService) {
        this.markDao = markDao;
        this.resultDao = resultDao;
        this.studentDao = studentDao;
        this.subjectDao = subjectDao;
        this.calculationService = calculationService;
    }

    /**
     * Retrieves mark entries for a student and semester, pre-populating with subjects
     * configured for that semester if marks were not yet entered.
     */
    public List<MarkEntry> getMarksForEntry(int studentId, int semester) throws SQLException {
        List<MarkEntry> existing = markDao.findByStudentAndSemester(studentId, semester);
        if (!existing.isEmpty()) {
            return existing;
        }

        // If no marks exist yet, build blank entries for all subjects in that semester
        List<Subject> subjects = subjectDao.findBySemester(semester);
        List<MarkEntry> list = new ArrayList<>();
        for (Subject sub : subjects) {
            MarkEntry entry = new MarkEntry();
            entry.setStudentId(studentId);
            entry.setSubjectId(sub.getId());
            entry.setSemester(semester);
            entry.setSubjectCode(sub.getSubjectCode());
            entry.setSubjectName(sub.getSubjectName());
            entry.setMaxMarks(sub.getMaxMarks());
            entry.setPassMarks(sub.getPassMarks());
            entry.setCredits(sub.getCredits());
            entry.setAbsent(false);
            entry.setMarksObtained(null);
            list.add(entry);
        }
        return list;
    }

    /**
     * Validates inputs, saves student marks batch, evaluates totals and grades,
     * and persists the computed result in SQLite.
     *
     * @param studentId ID of the student
     * @param semester Semester number
     * @param entries List of subject mark entries
     * @return Calculated Evaluation object
     */
    public CalculationService.Evaluation saveMarksAndCalculate(int studentId, int semester, List<MarkEntry> entries) 
            throws IllegalArgumentException, SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Please select a valid student.");
        }
        if (studentDao.findById(studentId) == null) {
            throw new IllegalArgumentException("Student with ID " + studentId + " does not exist in the database.");
        }
        if (!ValidationUtil.isValidSemester(semester)) {
            throw new IllegalArgumentException("Semester must be between 1 and 8.");
        }
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("No subjects configured for semester " + semester + ".");
        }

        // Validate each subject's mark entry and verify valid database foreign key references
        for (MarkEntry entry : entries) {
            entry.setStudentId(studentId);
            entry.setSemester(semester);

            // Defensive check: if subjectId is not positive, attempt to look up by subject code
            if (entry.getSubjectId() <= 0 && entry.getSubjectCode() != null) {
                Subject resolvedSub = subjectDao.findByCode(entry.getSubjectCode());
                if (resolvedSub != null) {
                    entry.setSubjectId(resolvedSub.getId());
                    if (entry.getMaxMarks() <= 0) entry.setMaxMarks(resolvedSub.getMaxMarks());
                    if (entry.getPassMarks() <= 0) entry.setPassMarks(resolvedSub.getPassMarks());
                }
            }

            if (entry.getSubjectId() <= 0) {
                throw new IllegalArgumentException("Invalid subject reference for " + entry.getSubjectCode() + ": Subject ID cannot be zero.");
            }

            // Verify subject exists in database to prevent foreign key violations
            Subject existingSubject = subjectDao.findById(entry.getSubjectId());
            if (existingSubject == null) {
                throw new IllegalArgumentException("Subject with ID " + entry.getSubjectId() + " (" + entry.getSubjectCode() + ") does not exist.");
            }

            if (!entry.isAbsent() && entry.getMarksObtained() != null) {
                double marks = entry.getMarksObtained();
                double max = entry.getMaxMarks();
                if (!ValidationUtil.isValidMarks(marks, max)) {
                    throw new IllegalArgumentException("Marks for " + entry.getSubjectCode() + 
                        " must be between 0.0 and " + max + ".");
                }
            }
        }

        // 1. Save marks into SQLite
        markDao.saveMarksBatch(entries);

        // 2. Compute evaluation (totals, percentage, pass/fail/incomplete status, grade)
        CalculationService.Evaluation evaluation = calculationService.evaluate(entries);

        // 3. Persist overall semester result into SQLite
        Result result = evaluation.toResult(studentId, semester);
        resultDao.saveResult(result);

        return evaluation;
    }

    /**
     * Searches and filters saved results.
     */
    public List<ResultSummary> searchResults(String query, Integer semester, String status) throws SQLException {
        return resultDao.searchAndFilter(query, semester, status);
    }

    /**
     * Retrieves summary result for report card.
     */
    public ResultSummary getReportCardSummary(int studentId, int semester) throws SQLException {
        return resultDao.getSummaryForStudent(studentId, semester);
    }

    /**
     * Retrieves detailed subject-wise marks for report card.
     */
    public List<MarkEntry> getReportCardMarks(int studentId, int semester) throws SQLException {
        return markDao.findByStudentAndSemester(studentId, semester);
    }

    /**
     * Deletes a result record.
     */
    public boolean deleteResult(int resultId) throws SQLException {
        return resultDao.deleteResult(resultId);
    }

    /**
     * Recalculates all results in the database (e.g. after subject rules change).
     */
    public int recalculateAllResults() throws SQLException {
        List<ResultSummary> summaries = resultDao.findAllSummaries();
        int count = 0;
        for (ResultSummary s : summaries) {
            List<MarkEntry> marks = markDao.findByStudentAndSemester(s.getStudentId(), s.getSemester());
            if (!marks.isEmpty()) {
                CalculationService.Evaluation eval = calculationService.evaluate(marks);
                Result res = eval.toResult(s.getStudentId(), s.getSemester());
                resultDao.saveResult(res);
                count++;
            }
        }
        return count;
    }

    /**
     * Summary dashboard metrics.
     */
    public int getTotalResultsCount() throws SQLException {
        return resultDao.countTotalResults();
    }

    public int getPassedResultsCount() throws SQLException {
        return resultDao.countPassedResults();
    }

    public int getFailedResultsCount() throws SQLException {
        return resultDao.countFailedResults();
    }

    public int getIncompleteResultsCount() throws SQLException {
        return resultDao.countIncompleteResults();
    }
}

