package jdbc_lesson.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdbc_lesson.entities.BaseEntity;
import lombok.SneakyThrows;

public class RepositoryJdbcImpl<E extends BaseEntity<ID>, ID> implements BaseRepository<E, ID>, Closeable {

    private final ObjectMapper mapper = new ObjectMapper();

    private final Connection connection;

    private final String tableName;

    private final Class<E> entityClass;

    public RepositoryJdbcImpl(Connection connection, Class<E> entityClass) {
        this.connection = connection;
        this.entityClass = entityClass;
        this.tableName = entityClass.getSimpleName().toLowerCase();
    }

    @SneakyThrows
    @Override
    public Optional<E> findById(ID id) {
        if (id == null) return Optional.empty();
        String query = "SELECT * FROM " + tableName + " WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, id);
            return extractSingleResult(toEntity(statement.executeQuery()));
        }
    }

    @SneakyThrows
    @Override
    public List<E> findAll() {
        String query = "SELECT * FROM " + tableName;
        try (PreparedStatement statement = connection.prepareStatement(query)){
            return toEntity(statement.executeQuery());
        }
    }
    
    @SneakyThrows
    @Override
    public void deleteById(ID id) {
        String sql = "DELETE FROM " + tableName + " WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        }
    }
    
    @SneakyThrows
    @Override
    public E save(E entity) {
        return findById(entity.getId())
                .map(e -> update(entity))
                .orElseGet(() -> create(entity));
    }

    @SneakyThrows
    @Override
    public List<E> saveAll(Iterable<E> itrb) {
        return StreamSupport.stream(itrb.spliterator(), false)
                .map(this::save)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private E update(E entity) {
        Map<String, Object> params = mapper.convertValue(entity, Map.class);
        String questionMarks = params.keySet().stream().collect(Collectors.joining(" = ?, ", "", " = ?"));
        String sql = "UPDATE " + tableName + " SET " + questionMarks + " WHERE id = " + entity.getId();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int index = 1;
            for (String key : params.keySet()) preparedStatement.setObject(index++, params.get(key));
            preparedStatement.executeUpdate();
        }
        return entity;
    }

    @SneakyThrows
    private E create (E entity) {
        Map<String, Object> params = new LinkedHashMap<>(mapper.convertValue(entity, Map.class));
        String values = params.keySet().stream().collect(Collectors.joining(", ", "(", ")"));
        String questionMarks = Stream.generate(()->"?").limit(params.size()).collect(Collectors.joining(", ", "(", ")"));
        String sql = "insert into " + tableName + " " +  values +  " values " + questionMarks;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            for (Object v : params.values()) preparedStatement.setObject(index++, v);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) throw new RuntimeException("Creating user failed, no rows affected.");
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) entity.setId((ID)generatedKeys.getObject(1));
                else throw new RuntimeException("Creating user failed, no ID obtained.");
            }
        }
        return entity;
    }

    @SneakyThrows
    private List<E> toEntity(ResultSet rs) {
        List<E> result = new ArrayList<>();
        while (rs.next()) {
            int columnCount = rs.getMetaData().getColumnCount();
            Map<String, Object> map = new HashMap<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                String columnLabel = rs.getMetaData().getColumnName(i);
                map.put(columnLabel, rs.getObject(columnLabel));
            }
            result.add(mapper.convertValue(map, entityClass));
        }
        return result;
    }
    
    private Optional<E> extractSingleResult(List<E> entities) {
        if (entities.size() > 1) throw new RuntimeException("The result is not unique");
        if (entities.isEmpty()) return Optional.empty();
        return Optional.of(entities.get(0));
    }
    
    @Override
    public void close() throws IOException {
        try{
            connection.close();
        } catch (Exception ex) {

        }
    }

}
