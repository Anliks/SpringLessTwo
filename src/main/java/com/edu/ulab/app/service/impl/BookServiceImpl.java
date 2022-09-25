package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.repository.BookRepository;
import com.edu.ulab.app.repository.UserRepository;
import com.edu.ulab.app.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository, UserRepository userRepository,
                           BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.userRepository = userRepository;
    }

    @Override
    public BookDto createBook(BookDto bookDto) {
        Book book = bookMapper.bookDtoToBook(bookDto);
        log.info("Mapped book: {}", book);
        Book savedBook = bookRepository.save(book);
        log.info("Saved book: {}", savedBook);
        return bookMapper.bookToBookDto(savedBook);
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        BookDto findBook = getBookById(bookDto.getId());
        log.info("get book {}", findBook);
        findBook.setAuthor(bookDto.getAuthor());
        findBook.setTitle(bookDto.getTitle());
        findBook.setPageCount(bookDto.getPageCount());
        log.info("update book {}", findBook);
        BookDto resultBook = bookMapper.bookToBookDto(bookRepository.save(bookMapper.bookDtoToBook(findBook)));
        log.info("book updated {}", resultBook);
        return resultBook;
    }

    @Override
    public BookDto getBookById(Long id) {
        BookDto bookDto = bookMapper.bookToBookDto(bookRepository.findById(id).orElseThrow(() -> new NotFoundException("not found book in base")));
        log.info("Got book by id {}", bookDto);
        return bookDto;
    }

    @Override
    public void deleteBookById(Long id) {
        BookDto book = getBookById(id);
        if (bookRepository.existsById(id)) {
            log.info("Got Book for delete {}", book);
            bookRepository.delete(bookMapper.bookDtoToBook(book));
        } else {
            throw new NotFoundException("Id Book Not found in Base " + id);
        }
    }

    @Override
    public List<BookDto> listBooksFromUser(Long id) {
        return bookRepository.findAll().stream().filter(book -> book.getUserId().equals(id)).map(bookMapper::bookToBookDto).collect(Collectors.toList());
    }

    @Override
    public void deleteBooksByUserId(Long userId) {
        listBooksFromUser(userId).stream().map(BookDto::getId).peek(id -> log.info("Deleted book/s  ids {}", id))
                .forEach(bookRepository::deleteById);
    }


}
