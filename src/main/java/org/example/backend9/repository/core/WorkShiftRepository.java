package org.example.backend9.repository.core;

import org.example.backend9.entity.core.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, Integer> {
}