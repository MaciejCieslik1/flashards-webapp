package com.PAP_team_21.flashcards.authentication.AuthenticationService;

import com.PAP_team_21.flashcards.Errors.AlreadyVerifiedException;
import com.PAP_team_21.flashcards.Errors.ResourceNotFoundException;
import com.PAP_team_21.flashcards.authentication.AuthenticationEmailSender.AuthenticationEmailSender;
import com.PAP_team_21.flashcards.entities.customer.Customer;
import com.PAP_team_21.flashcards.entities.customer.CustomerRepository;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevel;
import com.PAP_team_21.flashcards.entities.folderAccessLevel.FolderAccessLevelRepository;
import com.PAP_team_21.flashcards.entities.sentVerificationCodes.SentVerificationCode;
import com.PAP_team_21.flashcards.entities.sentVerificationCodes.SentVerificationCodeRepository;
import com.PAP_team_21.flashcards.entities.userPreferences.UserPreferences;
import com.PAP_team_21.flashcards.entities.userPreferences.UserPreferencesRepository;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatistics;
import com.PAP_team_21.flashcards.entities.userStatistics.UserStatisticsRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;
    private final FolderAccessLevelRepository folderAccessLevelRepository;
    private final SentVerificationCodeRepository sentVerificationCodeRepository;
    private final AuthenticationEmailSender emailSender;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserStatisticsRepository userStatisticsRepository;


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();


    @Value("${jwt.token-valid-time}")
    private long JwtTokenValidTime;

    @Value("${jwt.secret-key}")
    private String jwtSecret;

    @Value("${verification-code.length}")
    private int verificationCodeLength;

    @Value("${verification-code.expiration-minutes}")
    private int verificationCodeExpirationMinutes;

    public void registerUser(String email, String name,  String password) throws RuntimeException, MessagingException{
        verifyEmailCorrectness(email);
        String passwordHash = passwordEncoder.encode(password);


        Customer customer = new Customer(email, name, passwordHash);
        customer.setEnabled(false);
        customer.setProfileCreationDate(LocalDateTime.now());

        FolderAccessLevel al = customer.getFolderAccessLevels().get(0);

        Optional<Customer> opt = customerRepository.findByEmail(email);
        if(opt.isPresent())
        {
            throw new RuntimeException("user already exists");
        }
        al = folderAccessLevelRepository.save(al);

        UserPreferences userPreferences = new UserPreferences(al.getCustomer().getId(), false, 1);
        UserStatistics userStatistics = new UserStatistics(al.getCustomer().getId(), 0, 0, LocalDateTime.now());


        userPreferencesRepository.save(userPreferences);
        userStatisticsRepository.save(userStatistics);


        handleVerificationLink(customer);
    }

    public String loginUser(String email, String password) throws AuthenticationException {
        verifyEmailCorrectness(email);
        String token =" ";
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(
                email, password
        );

        authentication = authenticationManager.authenticate(authentication);

        if(authentication != null && authentication.isAuthenticated())
        {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date issued = new Date(System.currentTimeMillis());

            token = Jwts.builder()
                    .issuer("flashcards")
                    .subject("JWT Token")
                    .claim("email", email)
                    .issuedAt(issued)
                    .expiration(new Date(issued.getTime() + JwtTokenValidTime))
                    .signWith(secretKey)
                    .compact();
        }

        return token;
    }

    public String convertOAuth2ToJWT(Authentication authentication)
    {
        if(authentication instanceof OAuth2AuthenticationToken)
        {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getName();

            Optional<Customer> userOptional = customerRepository.findByEmail(email);
            Customer user;
            if(userOptional.isPresent())
            {
                user = userOptional.get();
            } else {
                user = new Customer(name, email, null);
                user.setProfileCreationDate(LocalDateTime.now());
                user = customerRepository.save(user);
            }


            Date issued = new Date(System.currentTimeMillis());
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            return Jwts.builder()
                    .issuer("flashcards")
                    .subject("JWT Token")
                    .claim("email", email)
                    .issuedAt(issued)
                    .expiration(new Date(issued.getTime() + JwtTokenValidTime))
                    .signWith(secretKey)
                    .compact();
        }
        else
        {
            throw new RuntimeException("not an OAuth2 token provided");
        }
    }

    private Customer extractCustomer(Authentication authentication) throws ResourceNotFoundException
    {
        String email = authentication.getName();

        Optional<Customer> customerOptional = customerRepository.findByEmail(email);

        if(customerOptional.isEmpty())
        {
            throw new RuntimeException("customer not found");
        }
        return customerOptional.get();
    }

    public void verifyUser(String email, String code) throws RuntimeException
    {
        verifyEmailCorrectness(email);
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);
        if(customerOpt.isEmpty())
            throw new RuntimeException("customer not found");
        Customer customer = customerOpt.get();

        SentVerificationCode verificationCode = customer.getSentVerificationCode();

        if(verificationCode == null)
        {
            throw new RuntimeException("verification code not found");
        }

        if(customer.isEnabled())
        {
            throw new AlreadyVerifiedException("user already verified");
        }

        if(customer.getSentVerificationCode().check(code))
        {
            customer.setEnabled(true);
            customer.setSentVerificationCode(null);
            sentVerificationCodeRepository.delete(verificationCode);
            customerRepository.save(customer);

        }
        else
        {
            throw new RuntimeException("verification code is incorrect");
        }
    }

    public void forgotPasswordRequest(String email) throws RuntimeException, MessagingException
    {
        verifyEmailCorrectness(email);
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);

        if(customerOptional.isEmpty())
        {
            throw new RuntimeException("customer with this email not found");
        }

        handleVerificationCode(customerOptional.get());
    }

    public void forgotPassword(String email, String code, String newPassword) throws RuntimeException
    {
        verifyEmailCorrectness(email);
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);

        if(customerOptional.isEmpty())
        {
            throw new RuntimeException("customer with this email not found");
        }

        Customer customer = customerOptional.get();
        SentVerificationCode verificationCode = customer.getSentVerificationCode();

        if(verificationCode == null)
        {
            throw new RuntimeException("verification code not found");
        }

        if(verificationCode.check(code))
        {
            customer.setPasswordHash(passwordEncoder.encode(newPassword));
            customer.setSentVerificationCode(null);
            sentVerificationCodeRepository.delete(verificationCode);
            customerRepository.save(customer);
        }
        else
        {
            throw new RuntimeException("verification code is incorrect");
        }
    }

    public void changePassword(Authentication authentication, String oldPassword, String newPassword) throws RuntimeException {
        Customer customer = extractCustomer(authentication);

        if(passwordEncoder.matches(oldPassword, customer.getPasswordHash()))
        {
            customer.setPasswordHash(passwordEncoder.encode(newPassword));
            customerRepository.save(customer);
        }
        else
        {
            throw new RuntimeException("old password is incorrect");
        }
    }

    private void handleVerificationCode(Customer customer) throws  MessagingException
    {
        String code = generateVerificationCode(verificationCodeLength);


        SentVerificationCode verification = customer.getSentVerificationCode();
        if(verification == null)
        {
            verification = new SentVerificationCode(code, customer, verificationCodeExpirationMinutes);
        }
        else
        {
            verification.setCode(code);
            verification.newExpirationDate(verificationCodeExpirationMinutes);
        }

        sentVerificationCodeRepository.save(verification);
        emailSender.sendVerificationCodeEmail(customer.getEmail(), code);
    }

    private void handleVerificationLink(Customer customer) throws MessagingException {
        String code = generateVerificationCode(verificationCodeLength);


        SentVerificationCode verification = customer.getSentVerificationCode();
        if(verification == null)
        {
            verification = new SentVerificationCode(code, customer, verificationCodeExpirationMinutes);
        }
        else
        {
            verification.setCode(code);
            verification.newExpirationDate(verificationCodeExpirationMinutes);
        }

        sentVerificationCodeRepository.save(verification);
        emailSender.sendVerificationLink(customer.getEmail(), code);

    }
    private String generateVerificationCode(int generatedCodeLength)
    {
        StringBuilder code = new StringBuilder(generatedCodeLength);

        for(int i = 0; i < generatedCodeLength; i++)
        {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return code.toString();
    }

    public void resendVerificationCode(String email) throws RuntimeException, MessagingException{
        verifyEmailCorrectness(email);
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);

        if(customerOptional.isEmpty())
        {
            throw new RuntimeException("customer with this email not found");
        }

        handleVerificationCode(customerOptional.get());
    }

    public void resendVerificationLink(String email) throws RuntimeException, MessagingException{
        verifyEmailCorrectness(email);
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);

        if(customerOptional.isEmpty())
        {
            throw new RuntimeException("customer with this email not found");
        }

        handleVerificationLink(customerOptional.get());
    }

    public void verifyEmailCorrectness(String email) throws RuntimeException
    {
        if(!email.matches("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"))
        {
            throw new RuntimeException("email is incorrect");
        }
    }

    public void changeEmail(Authentication authentication, String newEmail) throws Exception
    {
        verifyEmailCorrectness(newEmail);
        Customer customer = extractCustomer(authentication);
        customer.setEmail(newEmail);
        customerRepository.save(customer);
    }
}
