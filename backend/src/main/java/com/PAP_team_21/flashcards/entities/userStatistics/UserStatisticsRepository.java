package com.PAP_team_21.flashcards.entities.userStatistics;

import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Integer> {

    @Query(value = "CALL get_github_style_chart_data(:userId)", nativeQuery = true)
    List<java.sql.Date> getGithubStyleChartData(@Param("userId") Integer userId);


    @Procedure(procedureName = "find_customers_last_review")
    Optional<java.sql.Date> findCustomersLastReview(@Param("customerId") int id);

}
