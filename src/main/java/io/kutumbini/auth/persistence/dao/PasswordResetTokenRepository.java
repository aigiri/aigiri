package io.kutumbini.auth.persistence.dao;

import java.util.Date;
import java.util.stream.Stream;

import io.kutumbini.auth.persistence.model.PasswordResetToken;
import io.kutumbini.auth.persistence.model.User;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenRepository {

	public PasswordResetToken findByToken(String token) {
		return null;
	}

	public PasswordResetToken findByUser(User user) {
		return null;
	}

	public Stream<PasswordResetToken> findAllByExpiryDateLessThan(Date now) {
		return null;
	}

	public void deleteByExpiryDateLessThan(Date now) {
	}

	public void deleteAllExpiredSince(Date now) {
	}

	public void delete(PasswordResetToken passwordToken) {
	}

	public void save(PasswordResetToken myToken) {
	}
}
