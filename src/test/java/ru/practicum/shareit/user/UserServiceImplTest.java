package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    UserService userService;
    List<User> list;

//    //Create mock object of BookDAL
//    mockedBookDAL = mock(BookDAL.class);
//
//    //Create few instances of Book class.
//    book1 = new Book("8131721019","Compilers Principles",
//                     Arrays.asList("D. Jeffrey Ulman","Ravi Sethi", "Alfred V. Aho", "Monica S. Lam"),
//            "Pearson Education Singapore Pte Ltd", 2008,1009,"BOOK_IMAGE");
//
//    book2 = new Book("9788183331630","Let Us C 13th Edition",
//                     Arrays.asList("Yashavant Kanetkar"),"BPB PUBLICATIONS", 2012,675,"BOOK_IMAGE");
//
//    //Stubbing the methods of mocked BookDAL with mocked data.
//    when(mockedBookDAL.getAllBooks()).thenReturn(Arrays.asList(book1, book2));
//    when(mockedBookDAL.getBook("8131721019")).thenReturn(book1);
//    when(mockedBookDAL.addBook(book1)).thenReturn(book1.getIsbn());
//    when(mockedBookDAL.updateBook(book1)).thenReturn(book1.getIsbn());
    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
        list = new ArrayList<>();
        User user1 = new User(1L, "Bob", "bob@gmail.com");
        User user2 = new User(2L, "John", "john@ya.ru");
        User user3 = new User(3L, "Mary", "mary@hotmail.com");
        list.add(user1);
        list.add(user2);
        list.add(user3);

    }

    @Test
    void getAllRight() {
        when(userRepository.findAll())
                .thenReturn(list);

        Assertions.assertEquals(3, userService.getAll().size());
        Assertions.assertEquals("John", userService.getAll().get(1).getName());
        Assertions.assertEquals(1, userService.getAll().get(0).getId());
        Assertions.assertEquals("mary@hotmail.com", userService.getAll().get(2).getEmail());
    }

//    @Test
//    void getAllWrong() {
//    }

    @Test
    void postUser() {
        User newUser = new User(4L, "New", "new@mail.ru");
        list.add(newUser);

        when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(list.get(3));

        when(userRepository.findAll())
                .thenReturn(list);

        userService.postUser(newUser);

        Assertions.assertEquals(4, userService.postUser(newUser).getId());
        Assertions.assertEquals(4, userService.getAll().size());
    }

//    @Test
//    void deleteUser() {
//    }
//
//    @Test
//    void patchUser() {
//    }
//
    @Test
    void getUser() {
        User newUser = new User(4L, "New", "new@mail.ru");
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(newUser));
        Assertions.assertEquals("New", userRepository.findById(2L).get().getName());
    }
}