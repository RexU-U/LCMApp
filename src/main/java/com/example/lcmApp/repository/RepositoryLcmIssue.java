package com.example.lcmApp.repository;

import com.example.lcmApp.entity.LcmIssue;
import com.example.lcmApp.entity.Material;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryLcmIssue extends JpaRepository<LcmIssue, Long> {
    
    //public List<LcmIssue> saveAll(List<LcmIssue> materialIssue);
    
}