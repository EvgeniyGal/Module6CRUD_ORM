package jdbc_lesson.repository;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jdbc_lesson.entities.BaseEntity;

public class RepositoryFactory implements Closeable{

    private static RepositoryFactory repositoryFactory;

    public synchronized static RepositoryFactory getInstance(Connection connection) {
        if (repositoryFactory == null) repositoryFactory = new RepositoryFactory(connection);
        return repositoryFactory;
    }


    private final Map<String, BaseRepository> baseRepositories;

    private final Connection connection;

    public RepositoryFactory(Connection connection) {
        this.connection = connection;
        this.baseRepositories = new ConcurrentHashMap<>();
    }

    public synchronized <E extends BaseEntity<ID>, ID > BaseRepository<E, ID> of(Class<E> entityClass) {
        if (!baseRepositories.containsKey(entityClass.getName())) {
            BaseRepository repository = new RepositoryJdbcImpl(connection, entityClass);
            baseRepositories.put(entityClass.getName(), repository);
        }
        return baseRepositories.get(entityClass.getName());
    }

    @Override
    public void close() throws IOException {
        for (BaseRepository baseRepository : baseRepositories.values()) {
            baseRepository.close();
        }
        try {
            connection.close();
        } catch (Exception ex) {
        }
    }

}
