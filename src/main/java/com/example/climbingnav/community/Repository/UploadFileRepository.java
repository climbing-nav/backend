package com.example.climbingnav.community.Repository;

import com.example.climbingnav.community.entity.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {

    @Query("select f from UploadFile f where f.post.id in :postIds")
    List<UploadFile> findFilesByPostIds(@Param("postIds") List<Long> postIds);

    List<UploadFile> findAllByPost_Id(Long postId);
}
