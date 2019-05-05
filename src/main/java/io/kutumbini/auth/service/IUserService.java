package io.kutumbini.auth.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import io.kutumbini.auth.persistence.model.GrantVerificationToken;
import io.kutumbini.auth.persistence.model.PasswordResetToken;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.auth.persistence.model.VerificationToken;
import io.kutumbini.auth.web.dto.UserDto;
import io.kutumbini.auth.web.error.UserAlreadyExistException;

public interface IUserService {

    User registerNewUserAccount(UserDto accountDto) throws UserAlreadyExistException;

    User getUser(String verificationToken);

    void saveRegisteredUser(User user);

    void deleteUser(User user);

    VerificationToken createVerificationTokenForUser(User user, String token);

    VerificationToken getVerificationToken(String VerificationToken);

    VerificationToken generateNewVerificationToken(String token);

    void createPasswordResetTokenForUser(User user, String token);

    User findUserByEmail(String email);

    PasswordResetToken getPasswordResetToken(String token);

    User getUserByPasswordResetToken(String token);

    Optional<User> getUserByID(long id);

    void changeUserPassword(User user, String password);

    boolean checkIfValidOldPassword(User user, String password);

    String validateVerificationToken(String token);

    String generateQRUrl(User user) throws UnsupportedEncodingException;

    User updateUser2FA(boolean use2FA);

    List<String> getUsersFromSessionRegistry();

    // for kutumbini
    GrantVerificationToken getGrantVerificationToken(String token);

	// for kutumbini
    GrantVerificationToken createGrantVerificationToken(User user, String token, String emailTo, short accessLevel);

}
