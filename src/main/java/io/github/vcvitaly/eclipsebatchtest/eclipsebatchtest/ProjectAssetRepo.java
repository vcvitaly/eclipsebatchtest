package io.github.vcvitaly.eclipsebatchtest.eclipsebatchtest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectAssetRepo extends JpaRepository<ProjectAsset, Long> {
}
