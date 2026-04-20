package com.yusuf.bist_portfolio_management.service;

import com.yusuf.bist_portfolio_management.dto.AuthResponse;
import com.yusuf.bist_portfolio_management.dto.LoginRequest;
import com.yusuf.bist_portfolio_management.dto.RegisterRequest;
import com.yusuf.bist_portfolio_management.entity.AppUser;
import com.yusuf.bist_portfolio_management.entity.TradingAccount;
import com.yusuf.bist_portfolio_management.exception.EmailAlreadyExistsException;
import com.yusuf.bist_portfolio_management.exception.UsernameAlreadyExistsException;
import com.yusuf.bist_portfolio_management.repository.AppUserRepository;
import com.yusuf.bist_portfolio_management.repository.TradingAccountRepository;
import com.yusuf.bist_portfolio_management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100000.0000");

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username already taken: " + request.getUsername());
        }

        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already registered: " + request.getEmail());
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        appUserRepository.save(user);

        TradingAccount account = TradingAccount.builder()
                .user(user)
                .cashBalance(INITIAL_BALANCE)
                .initialBalance(INITIAL_BALANCE)
                .build();

        tradingAccountRepository.save(account);

        String token = jwtTokenProvider.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(request.getUsername());
        return new AuthResponse(token, request.getUsername());
    }
}