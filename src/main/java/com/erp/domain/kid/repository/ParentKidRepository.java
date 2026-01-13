package com.erp.domain.kid.repository;

import com.erp.domain.kid.entity.ParentKid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentKidRepository extends JpaRepository<ParentKid, Long> {

    List<ParentKid> findByKidId(Long kidId);

    List<ParentKid> findByParentId(Long parentId);

    void deleteByKidId(Long kidId);

    void deleteByParentId(Long parentId);
}
