package jdbc_lesson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import jdbc_lesson.entities.People;
import jdbc_lesson.repository.BaseRepository;
import jdbc_lesson.repository.RepositoryFactory;
import lombok.SneakyThrows;

public class MySqlDbExample {

    public static final Properties PROPERTIES;

    static {
        PROPERTIES = loadProperties("application.properties");
    }

    private static Properties loadProperties(final String propertiesFile) {
        try (final InputStream is = new BufferedInputStream(MySqlDbExample.class
                .getClassLoader().getResourceAsStream(propertiesFile))) {
            final Properties property = new Properties();
            property.load(is);
            return property;
        } catch (NullPointerException | IOException ex) {
            return null;
        }
    }

    @SneakyThrows
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        try (Connection conn = DriverManager.getConnection(PROPERTIES.getProperty("url"), PROPERTIES); 
                RepositoryFactory repositoryFactory = RepositoryFactory.getInstance(conn)) {
            BaseRepository<People, BigInteger> repository = repositoryFactory.of(People.class);
            Optional<People> findById = repository.findById(new BigInteger("1"));
            System.out.println(findById);
            List<People> findById1 = repository.findAll();
            System.out.println(findById1);
            People p1 = People.builder().age(10).name("Oleg").birthday("10.01.2003").build();
            repository.save(p1);
        }
    }

}
