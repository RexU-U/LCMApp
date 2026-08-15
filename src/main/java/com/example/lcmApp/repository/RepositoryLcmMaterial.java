package com.example.lcmApp.repository;

import com.example.lcmApp.entity.Material;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryLcmMaterial extends JpaRepository<Material, String> {

    @Override
    List<Material> findAll();

    Optional<Material> findByName(String name);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Material m SET m.volume = COALESCE(m.volume, 0) + :volume WHERE m.name = :name")
    int updateVolumeByName(@Param("name") String name,
                           @Param("volume") Double volume);

    @Modifying(clearAutomatically = true)
@Transactional
@Query("UPDATE Material m SET " +
       "m.name = :name, " + // добавили обновление имени
       "m.description = :description, " +
       "m.volume = :volume, " +
       "m.unit = :unit, " +
       "m.type = :type, " +
       "m.priority = :priority, " +
       "m.inventory = :inventory " +
       "WHERE m.material_id = :material_id")
int updateMaterial(
    @Param("material_id") Long material_id,
    @Param("name") String name,
    @Param("description") String description,
    @Param("volume") Double volume,
    @Param("unit") String unit,
    @Param("type") String type,
    @Param("priority") Double priority,
    @Param("inventory") Double inventory
);

    
    @Query("SELECT m.name from Material m WHERE m.type = :category")
    List<String> getMaterialsNameByCategory(@Param("category") String category);
    
    @Transactional
    Material save(Material material);
    
    @Transactional
    int deleteByName(String name);
}
