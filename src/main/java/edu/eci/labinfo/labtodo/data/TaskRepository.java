package edu.eci.labinfo.labtodo.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import edu.eci.labinfo.labtodo.model.Task;
import edu.eci.labinfo.labtodo.model.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    boolean existsById(Long taskId);
    List<Task> findByUsersUserId(Long userId);
    List<Task> findByTypeTask(String typeTask);
    List<Task> findByStatus(String status);
    List<Task> findByUsersUserIdAndStatusAndSemesterPeriodId(Long userId, String status, Long periodId);
    List<Task> findByTypeTaskAndStatusAndSemesterPeriodId(String typeTask, String status, Long periodId);
    List<Task> findBySemesterPeriodId(Long periodId);
    List<Task> findByTypeTaskAndSemesterPeriodId(String typeTask, Long periodId);

    @Query("SELECT t FROM Task t JOIN t.users u WHERE u.userId = :userId AND t.status = :status")
    List<Task> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT t FROM Task t WHERE t.typeTask = :typeTask AND  t.status = :status")
    List<Task> findByTypeAndStatus(@Param("typeTask") String typeTask, @Param("status") String status);

    @Query("SELECT DISTINCT c.creatorUser FROM Task t JOIN t.comments c WHERE t.taskId = :taskId")
    List<User> findUsersWhoCommented(Long taskId);

    List<Task> findByTypeTaskAndStatusNot(String typeTask, String excludedStatus);

}
