package com.PAP_team_21.flashcards.controllers;

import com.PAP_team_21.flashcards.authentication.AuthenticationRequest;
import com.PAP_team_21.flashcards.authentication.AuthenticationResponse;
import com.PAP_team_21.flashcards.authentication.AuthenticationService.AuthenticationService;
import com.PAP_team_21.flashcards.authentication.RegisterRequest;
import com.PAP_team_21.flashcards.controllers.requests.*;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.login.Login;
import com.PAP_team_21.flashcards.entities.login.LoginRepository;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatistics;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final CustomerRepository customerRepository;
    private final LoginRepository loginRepository;
    private final UserStatisticsRepository userStatisticsRepository;

    @GetMapping("/oauth2/success")
    public  ResponseEntity<?> oauth2Success(Authentication authentication) {
        try {
            return ResponseEntity.ok(new AuthenticationResponse(authenticationService.convertOAuth2ToJWT(authentication)));
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception occurred: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestBody RegisterRequest request
    )
    {
        try{
            authenticationService.registerUser(request.getEmail(), request.getUsername(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body("customer registered");
        }
        catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception occurred: " + e.getMessage());
        }
    }

    @PostMapping("/usernamePasswordLogin")
    public ResponseEntity<?> usernamePasswordLogin(
            @RequestBody AuthenticationRequest request
    ){
        try{
            String email = request.getEmail();
            Optional<Customer> customerOpt = customerRepository.findByEmail(email);
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                Login login = new Login(customer.getId());
                loginRepository.save(login);

                List<LocalDate> loginDates = userStatisticsRepository.getGithubStyleChartData(customer.getId())
                        .stream()
                        .map(java.sql.Date::toLocalDate)
                        .collect(Collectors.toList());

                UserStatistics userStatistics = customer.getUserStatistics();
                userStatistics.updateStreak(loginDates);
                userStatisticsRepository.save(userStatistics);
            }
            return ResponseEntity.ok(new AuthenticationResponse(authenticationService.loginUser(request.getEmail(), request.getPassword())));
        } catch(AuthenticationException e)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/validateToken")
    public ResponseEntity<?> validate(Authentication authentication)
    {
        return ResponseEntity.ok("token is valid");
    }


    @PostMapping("/verifyUser")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserRequest request)
    {
        try {
            authenticationService.verifyUser(request.getEmail(), request.getCode());
            return ResponseEntity.ok("user verified successfully");
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception occurred: " + e.getMessage());
        }

    }

    @PostMapping("/resendVerificationCode")
    public ResponseEntity<?> resendVerificationCode(@RequestBody ResendVerificationCodeRequest request)
    {
        try {
            authenticationService.resendVerificationCode(request.getEmail());
            return ResponseEntity.ok("verification code resent");
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception occurred: " + e.getMessage());
        }
    }

    @PostMapping("/resendVerificationLink")
    public ResponseEntity<?> resendVerificationLink(@RequestBody ResendVerificationCodeRequest request)
    {
        try {
            authenticationService.resendVerificationLink(request.getEmail());
            return ResponseEntity.ok("verification link resent");
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception occurred: " + e.getMessage());
        }
    }

    @PostMapping("/forgotPasswordRequest")
    public ResponseEntity<?> forgotPasswordRequest(@RequestBody ForgotPasswordRequest request)
    {
        try {
            authenticationService.forgotPasswordRequest(request.getEmail());
            return ResponseEntity.ok("password reset request sent");
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception occurred: " + e.getMessage());
        }
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody NewPasswordAfterForgetRequest request)
    {
        try {
            authenticationService.forgotPassword(request.getEmail(), request.getCode(), request.getNewPassword());
            return ResponseEntity.ok("password reset successful");
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception occurred: " + e.getMessage());
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request)
    {
        try {
            authenticationService.changePassword(authentication, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("password changed successfully");
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception occurred: " + e.getMessage());
        }
    }

    @PostMapping("/changeEmail")
    public ResponseEntity<?> changeEmail(Authentication authentication, @RequestBody ChangeEmailRequest request)
    {
        try {
            authenticationService.changeEmail(authentication, request.getNewEmail());
            return ResponseEntity.ok("email changed successfully");
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("exception occurred: " + e.getMessage());
        }
    }

}
