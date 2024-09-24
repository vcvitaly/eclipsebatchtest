package io.github.vcvitaly.eclipsebatchtest.eclipsebatchtest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "project_asset")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectAsset {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "project_asset_id_seq"
    )
    @SequenceGenerator(
            name = "project_asset_id_seq",
            sequenceName = "project_asset_id_seq",
            allocationSize = 300
    )
    @EqualsAndHashCode.Exclude
    @Column(name = "id", nullable = false)
    private Long id;
    private String projectGuid;
    private String assetGuid;
}
