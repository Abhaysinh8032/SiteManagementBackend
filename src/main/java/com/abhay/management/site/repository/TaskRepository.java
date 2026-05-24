package com.abhay.management.site.repository;

import com.abhay.management.site.entity.Task;
import com.abhay.management.site.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllBySiteId(UUID siteId);

    List<Task> findAllByAssignedToId(UUID userId);

    List<Task> findAllBySiteIdAndStatus(UUID siteId, TaskStatus status);
}
