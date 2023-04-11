package jdbc_lesson.repository;

import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import jdbc_lesson.entities.BaseEntity;

public interface BaseRepository<E extends BaseEntity<ID>, ID> extends Closeable{

    Optional<E> findById(ID id);

    List<E> findAll();

    E save(E entity);

    List<E> saveAll(Iterable<E> itrb);
    
    void deleteById(ID id);
}
