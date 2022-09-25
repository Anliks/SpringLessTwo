package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.exception.NotValidDataException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class BookServiceImplTemplate implements BookService {

    private final JdbcTemplate jdbcTemplate;
    BookMapper bookMapper;

    public BookServiceImplTemplate(JdbcTemplate jdbcTemplate, BookMapper bookMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookMapper = bookMapper;
    }

    @Override
    public BookDto createBook(BookDto bookDto) {
        final String INSERT_SQL = "INSERT INTO BOOK(TITLE, AUTHOR, PAGE_COUNT, USER_ID) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps =
                                connection.prepareStatement(INSERT_SQL, new String[]{"id"});
                        ps.setString(1, bookDto.getTitle());
                        ps.setString(2, bookDto.getAuthor());
                        ps.setLong(3, bookDto.getPageCount());
                        ps.setLong(4, bookDto.getUserId());
                        return ps;
                    }
                },
                keyHolder);

        bookDto.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return bookDto;
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        final String UPDATE_SQL = "UPDATE BOOK SET TITLE = ?, AUTHOR = ?, PAGE_COUNT = ? WHERE USER_ID = ?";
        if (bookDto.getId() == null) {
            throw new NotFoundException("Id not found ");
        }
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(UPDATE_SQL);
                ps.setString(1, bookDto.getTitle());
                ps.setString(2, bookDto.getAuthor());
                ps.setLong(3, bookDto.getPageCount());
                ps.setLong(4, bookDto.getUserId());
                log.info("Book updated " + bookDto.getId());
                return ps;
            }
        });
        return bookDto;
    }

    @Override
    public BookDto getBookById(Long id) {
        final String SELECT_SQL = "SELECT * FROM BOOK WHERE ID = ?";
        Book book = jdbcTemplate.queryForObject(SELECT_SQL, new RowMapper<Book>() {
            @Override
            public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
                Book book = new Book();
                book.setId(rs.getLong("ID"));
                book.setAuthor(rs.getString("AUTHOR"));
                book.setTitle(rs.getString("TITLE"));
                book.setPageCount(rs.getInt("PAGE_COUNT"));
                book.setUserId(rs.getLong("USER_ID"));
                return book;
            }
        });

        return bookMapper.bookToBookDto(book);
    }

    @Override
    public void deleteBookById(Long id) {
        if (id == null) {
            throw new NotValidDataException("id is null");
        }
        final String DELETE_SQL = "DELETE * FROM BOOK WHERE ID = ?";
        jdbcTemplate.update(DELETE_SQL, id);
    }

    @Override
    public List<BookDto> listBooksFromUser(Long id) {
        final String GET_BOOKS_SQL = "SELECT * FROM BOOK WHERE USER_ID = ? ";
        return jdbcTemplate.query(GET_BOOKS_SQL, (rs, rowNum) -> new BookDto(rs.getLong("id"), rs.getLong("user_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getLong("page_count")), id);

    }

    @Override
    public void deleteBooksByUserId(Long userId) {
        final String GET_LIST_BOOKS = "DELETE FROM BOOK WHERE USER_ID = ?";
        if (userId == null) {
            throw new NotValidDataException("Id is null");
        }
        jdbcTemplate.update(GET_LIST_BOOKS, userId);
    }

}
