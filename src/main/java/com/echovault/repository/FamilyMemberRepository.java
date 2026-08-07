package com.echovault.repository;

import com.echovault.model.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    List<FamilyMember> findByUserId(Long userId);

    List<FamilyMember> findByUserIdAndPermissionLevel(Long userId, FamilyMember.PermissionLevel permissionLevel);

    @Query("SELECT f FROM FamilyMember f WHERE f.user.id = :ownerId AND f.permissionLevel = :permissionLevel")
    List<FamilyMember> findByOwnerIdAndPermissionLevel(@Param("ownerId") Long ownerId, 
                                                        @Param("permissionLevel") FamilyMember.PermissionLevel permissionLevel);
}
