package nextstep.jdbc;

import slipp.support.db.ConnectionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {
    private final Connection connection;

    public JdbcTemplate(Connection connection) {
        this.connection = connection;
    }

    public void insert(String sql, PrepareStatementSetter prepareStatementSetter) throws SQLException {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            prepareStatementSetter.setParameters(preparedStatement);
            preparedStatement.executeUpdate();
        }
    }

    public void insert(String sql, Object... args) throws SQLException {
        PrepareStatementSetter prepareStatementSetter = getPrepareStatementSetter(args);
        insert(sql, prepareStatementSetter);
    }

    public <T> T objectQuery(String sql, RowMapper<T> rowMapper, PrepareStatementSetter prepareStatementSetter) throws SQLException {
        try (ResultSet resultSet = getResultSet(sql, prepareStatementSetter, this.connection)) {
            return rowMapper.mapRow(resultSet);
        }
    }

    public <T> T objectQuery(String sql, RowMapper<T> rowMapper, Object... args) throws SQLException {
        PrepareStatementSetter prepareStatementSetter = getPrepareStatementSetter(args);
        return objectQuery(sql, rowMapper, prepareStatementSetter);
    }

    public <T> List<T> listQuery(String sql, RowMapper<T> rowMapper, Object... args) throws SQLException {
        PrepareStatementSetter prepareStatementSetter = getPrepareStatementSetter(args);
        return listQuery(sql, rowMapper, prepareStatementSetter);
    }

    public <T> List<T> listQuery(String sql, RowMapper<T> rowMapper, PrepareStatementSetter prepareStatementSetter) throws SQLException {
        List<T> objects = new ArrayList<>();
        try (ResultSet resultSet = getResultSet(sql, prepareStatementSetter, this.connection)) {
            while (resultSet.next()) {
                objects.add(rowMapper.mapRow(resultSet));
            }
        }
        return objects;
    }

    private ResultSet getResultSet(String sql, PrepareStatementSetter prepareStatementSetter, Connection con) throws SQLException {
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        prepareStatementSetter.setParameters(preparedStatement);
        return preparedStatement.executeQuery();
    }

    private PrepareStatementSetter getPrepareStatementSetter(Object[] args) {
        return preparedStatement -> {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
        };
    }
}
