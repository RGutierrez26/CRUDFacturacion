package com.TpGrupalSpring.TpGrupal.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface AudotoriaRepository<T, ID> extends JpaRepository<T, ID> {

    List<T> findByFechaBajaIsNull();

    Optional<T> findByIdAndFechaBajaIsNull(ID id);

}
