package com.pms.service;

import com.pms.model.Drive;
import com.pms.model.Student;
import org.springframework.stereotype.Service;

/**
 * Core business logic of the platform: given a drive's eligibility
 * criteria (minimum percentage, branch whitelist, max backlogs allowed),
 * decide whether a given student may apply.
 */
@Service
public class EligibilityService {

    public boolean isEligible(Student student, Drive drive) {
        if (student == null || drive == null) {
            return false;
        }
        return meetsPercentage(student, drive)
                && meetsBacklogs(student, drive)
                && meetsBranch(student, drive);
    }

    public boolean meetsPercentage(Student student, Drive drive) {
        if (student.getPercentage() == null || drive.getMinPercentage() == null) {
            return false;
        }
        return student.getPercentage() >= drive.getMinPercentage();
    }

    public boolean meetsBacklogs(Student student, Drive drive) {
        if (student.getBacklogs() == null || drive.getMaxBacklogs() == null) {
            return false;
        }
        return student.getBacklogs() <= drive.getMaxBacklogs();
    }

    public boolean meetsBranch(Student student, Drive drive) {
        // An empty/null eligible-branches set means the drive is open to all branches.
        if (drive.getEligibleBranches() == null || drive.getEligibleBranches().isEmpty()) {
            return true;
        }
        if (student.getBranch() == null) {
            return false;
        }
        return drive.getEligibleBranches().stream()
                .anyMatch(b -> b.equalsIgnoreCase(student.getBranch()));
    }
}
