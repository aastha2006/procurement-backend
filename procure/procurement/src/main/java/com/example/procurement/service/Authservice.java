package com.example.procurement.service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.procurement.DTO.UpdateUserLoginDTO;
import com.example.procurement.entity.AppUser;
import com.example.procurement.entity.Group;
import com.example.procurement.entity.RefreshToken;
import com.example.procurement.entity.SocietyUser;
import com.example.procurement.entity.UserPasswordHistory;
import com.example.procurement.entity.Vendor;
import com.example.procurement.exception.InvalidcredentialException;
import com.example.procurement.exception.PasswordValidationException;
import com.example.procurement.exception.TempPasswordException;
import com.example.procurement.exception.UserNotFoundException;
import com.example.procurement.filters.JwtUtils;
import com.example.procurement.repository.GroupsRepo;
import com.example.procurement.repository.RefreshTokenRepo;
import com.example.procurement.repository.RolesRepo;
import com.example.procurement.repository.SocietyUserRepository;
import com.example.procurement.repository.UserLoginRepo;
import com.example.procurement.repository.UserPasswordHistoryRepo;
import com.example.procurement.repository.VendorRepository;

@Service
public class Authservice {

    @Autowired
    UserLoginRepo userLoginRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    GroupsRepo grouprolesRepo;

    @Autowired
    UserPasswordHistoryRepo userPasswordHistoryRepo;

    @Autowired
    RolesRepo rolesRepo;

    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    RefreshTokenRepo refreshTokenRepository;

    // @Autowired
    // TemplateEngine templateEngine;

    // @Autowired
    // MailService mailService;

    @Autowired
    VendorRepository vendorRepo;

    @Autowired
    SocietyUserRepository employeeRepo;

    @Value("${UIresetUrl}")
    private String resetUrlUI;

    public Object getloggedUserDetail(String email, String loginType) {
        if (loginType.equalsIgnoreCase("Supplier")) {
            Vendor vendor = vendorRepo.findByEmail(email)
                    .orElseThrow(() -> new NoSuchElementException("Vendor doesn't exists with this email " + email));
            return vendor;
        }
        // Search in employee repository
        SocietyUser employee = employeeRepo.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Employee doesn't exists with this email " + email));
        return employee;

    public Map<String, Object> getSessionDebugInfo(String username, String loginType) {
        AppUser user = userLoginRepo.findByUsernameAndLoginType(username, loginType);
        Map<String, Object> info = new HashMap<>();
        if (user != null) {
            info.put("username", user.getUsername());
            info.put("activeSessionId", user.getActiveSessionId());
            info.put("sessionExpiryEpoch", user.getSessionExpiryEpoch());
            info.put("currentServerEpoch", System.currentTimeMillis());
            info.put("activeSessionExpiry", user.getActiveSessionExpiry()); // Log old one too
        } else {
            info.put("error", "User not found");
        }
        return info;
    }

    public AppUser saveUserLogin(AppUser userlogin2) {
        AppUser userlogin = new AppUser();
        if (userlogin2.getLoginType().equalsIgnoreCase("Buyer")) {
            userlogin2.setIdVen(null);
        } else {
            userlogin2.setIdempuser(null);
        }
        if (userlogin2.getDt_enable() != null &&
                (userlogin2.getDt_enable().isBefore(LocalDate.now()) ||
                        userlogin2.getDt_enable().isEqual(LocalDate.now()))) {

            userlogin2.setEnabled(true);
        } else {
            userlogin2.setEnabled(false);
        }
        BeanUtils.copyProperties(userlogin2, userlogin);
        String encodedPassword = passwordEncoder.encode(userlogin2.getPassword());
        userlogin.setPassword(encodedPassword);
        AppUser userLogin3 = userLoginRepo.save(userlogin);

        // Save to password history
        savePasswordToHistory(userLogin3.getId().intValue(), encodedPassword);
        return userLogin3;
    }

    private void savePasswordToHistory(Integer userId, String encodedPassword) {
        UserPasswordHistory history = new UserPasswordHistory();
        history.setUserId(userId);
        history.setHashedPassword(encodedPassword);
        history.setChangedAt(LocalDateTime.now());

        userPasswordHistoryRepo.save(history);
    }

    private void validatePasswordPolicy(String password, String username, String email, Integer userId) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty.");
        } else {
            if (password.length() < 8) {
                errors.add("Password must be at least 8 characters long.");
            }

            String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$";
            if (!password.matches(pattern)) {
                errors.add("Password must include uppercase, lowercase, number, and special character.");
            }

            if ((username != null && password.toLowerCase().contains(username.toLowerCase())) ||
                    (email != null && password.toLowerCase().contains(email.toLowerCase()))) {
                errors.add("Password cannot contain username or email.");
            }

            if (password.toLowerCase().matches(".*(0123|1234|2345|abcd|bcde|cdef).*")) {
                errors.add("Password cannot contain sequential characters.");
            }

            if (password.matches(".*(.)\\1{3,}.*")) {
                errors.add("Password cannot contain repeated characters.");
            }

            Set<String> commonPasswords = Set.of("password123", "admin123", "qwerty", "12345678", "welcome");
            if (commonPasswords.contains(password.toLowerCase())) {
                errors.add("Password is too common. Choose a stronger one.");
            }

            if (userId != null) {
                List<UserPasswordHistory> last5 = userPasswordHistoryRepo.findTop5ByUserIdOrderByChangedAtDesc(userId);
                for (UserPasswordHistory oldPwd : last5) {
                    if (passwordEncoder.matches(password, oldPwd.getHashedPassword())) {
                        errors.add("Password cannot be same as the last 5 used.");
                        break;
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new PasswordValidationException(errors);
        }
    }

    // ** API for password-reset **/
    public void resetPasswordWithToken(String token, String newPassword) {
        AppUser user = userLoginRepo.findByResetToken(token);

        if (user == null || user.getTokenExpiryTime() == null
                || user.getTokenExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired token.");
        }

        // Checking password policy
        validatePasswordPolicy(user.getPassword(), user.getUsername(), user.getEmail(), user.getId().intValue());

        // Updating password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setResetToken(null);
        user.setTokenExpiryTime(null);
        user.setLastPasswordChangedAt(LocalDateTime.now());

        List<LocalDateTime> timestamps = user.getPasswordChangeTimestamps();
        if (timestamps == null) {
            timestamps = new ArrayList<>();
        }
        timestamps.add(LocalDateTime.now());
        user.setPasswordChangeTimestamps(timestamps);

        userLoginRepo.save(user);
        // Save to password history
        savePasswordToHistory(user.getId().intValue(), encodedPassword);
    }

    private boolean isPasswordValid(String password) {
        return password != null &&
                password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[@#$%^&+=!].*");
    }

    public Boolean updateUserLogin(UpdateUserLoginDTO ul, Integer empid) {

        Optional<AppUser> existingUserOpt = userLoginRepo.findByIdempuserId(empid);

        if (existingUserOpt.isPresent()) {
            return false;
        }

        AppUser existingUser = existingUserOpt.get();

        // Only update fields if present in DTO (null check)
        // if (ul.getTypePassword() != null) {
        // existingUser.setTypePassword(ul.getTypePassword());
        // }
        if (ul.getEnabled() != null) {
            existingUser.setEnabled(ul.getEnabled());
        }
        if (ul.getDt_enable() != null) {
            existingUser.setDt_enable(ul.getDt_enable());
        }
        if (ul.getDt_disable() != null) {
            existingUser.setDt_disable(ul.getDt_disable());
        }
        if (ul.getIdgroup() != null) {
            // Assuming Group is fetched based on id
            Group group = grouprolesRepo.findById(ul.getIdgroup().longValue())
                    .orElse(null);
            existingUser.setIdGroup(group);
        }

        userLoginRepo.save(existingUser);

        return true;
    }

    public Group getGrouproles(Integer id) {
        return grouprolesRepo.findById(id.longValue())
                .orElseThrow(() -> new NoSuchElementException("Group roles doesn't exist with this id" + id));
    }

    public List<Group> fetchGrouproles() {
        return grouprolesRepo.findAll();
    }

    public List<Group> createGroupRoles(List<Group> grouproles) {
        return grouprolesRepo.saveAll(grouproles);
    }

    @Transactional
    public Map<String, String> authenticateUser(String nmLogin, String password, String loginType) throws Exception {
        Map<String, String> token;

        AppUser user = userLoginRepo.findByUsernameAndLoginType(nmLogin, loginType);

        if (user != null) {
            // check for profile locked
            if (user.isAccountLocked()) {
                // Optional: unlock after 15 mins
                if (user.getLockTime() != null &&
                        user.getLockTime().plusMinutes(15).isBefore(LocalDateTime.now())) {
                    // Auto-unlock
                    user.setAccountLocked(false);
                    user.setFailedAttempts(0);
                    user.setLockTime(null);
                } else {
                    LocalDateTime lockExpiryTime = user.getLockTime().plusMinutes(15);
                    ZonedDateTime istTime = lockExpiryTime.atZone(ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                    throw new TempPasswordException("Account is locked. Try again after 15 mins i.e " + istTime
                            + " Or else continue Forget Password for reset.");
                }
            }
            if (passwordEncoder.matches(password, user.getPassword())) {
                // ✅ Check if it's a temporary password
                // if (user.getTypePassword().equalsIgnoreCase("Temporary")) {
                // throw new TempPasswordException("Temporary password used. Please change your
                // password.");
                // }
                // ✅ Successful login

                // SINGLE SESSION CHECK
                long now = System.currentTimeMillis();
                System.out.println("Processing Login for: " + nmLogin);
                System.out.println("Current Session ID: " + user.getActiveSessionId());
                System.out.println("Current Epoch: " + user.getSessionExpiryEpoch());
                System.out.println("Server Now: " + now);

                if (user.getActiveSessionId() != null &&
                        user.getSessionExpiryEpoch() != null &&
                        user.getSessionExpiryEpoch() > now) {
                    System.out.println("LOGIN DENIED: Session Active and Not Expired.");
                    throw new RuntimeException(
                            "Login denied: User already logged in on another device. Please logout from the other device first or wait for the session to expire.");
                }

                user.setFailedAttempts(0);

                // Set new session
                String newSessionId = UUID.randomUUID().toString();
                System.out.println("Generating New Session ID: " + newSessionId);
                user.setActiveSessionId(newSessionId);
                // 30 minutes * 60 seconds * 1000 millis
                user.setSessionExpiryEpoch(now + (30 * 60 * 1000));

                System.out.println("Saving user with new session (Epoch)...");
                userLoginRepo.saveAndFlush(user);
                System.out.println("User saved.");

                token = jwtUtils.generateJwtToken(user);
                // save the refresh token in database along with user
                saveRefreshToken(token, user);

                return token;

            } else {
                user.setFailedAttempts(user.getFailedAttempts() + 1);

                // Lock if 5 attempts reached
                if (user.getFailedAttempts() >= 5) {
                    user.setAccountLocked(true);
                    user.setLockTime(LocalDateTime.now());
                }

                userLoginRepo.save(user);

                if (user.getFailedAttempts() >= 3) {
                    throw new InvalidcredentialException("Invalid Credentials. Failed attempts: "
                            + user.getFailedAttempts()
                            + ". Warning: Account will be locked after 5 failed attempts. Consider using 'Forgot Password'.");
                }

                throw new InvalidcredentialException(" Invalid Credentials, Please check password !!");
            }

        } else
            throw new RuntimeException(" User doesn't exist !!");

    }

    public void logoutUser(String username, String loginType) {
        AppUser user = userLoginRepo.findByUsernameAndLoginType(username, loginType);
        if (user != null) {
            user.setActiveSessionId(null);
            user.setSessionExpiryEpoch(null); // Clear Epoch
            userLoginRepo.save(user);
        }
    }

    public void saveRefreshToken(Map<String, String> token, AppUser user) {
        // Safely convert String to Instant
        String expiryTimeStr = token.get("refreshExpirytime");
        Instant expiryTime = Instant.ofEpochMilli(Long.parseLong(expiryTimeStr));

        // Retrieve existing token if present
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId().intValue());

        if (refreshToken == null) {
            // Create new token
            refreshToken = new RefreshToken();
            refreshToken.setUser(user);
        }

        // Set or update token details
        refreshToken.setExpiryDate(expiryTime);
        refreshToken.setToken(token.get("refresh_token"));

        // Save the token
        refreshTokenRepository.save(refreshToken);
    }

    public Map<String, String> verifyRefreshToken(String refreshToken) {

        RefreshToken existrefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid Refresh Token"));

        if (existrefreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh Token Expired");
        }

        Map<String, String> token = jwtUtils.generateJwtToken(existrefreshToken.getUser());
        saveRefreshToken(token, existrefreshToken.getUser());

        return token;

    }

    public void updatePassword(String username, String newPassword) {

        AppUser updatUserLogin = userLoginRepo.findByUsername(username);
        validatePasswordPolicy(newPassword, updatUserLogin.getUsername(), updatUserLogin.getEmail(),
                updatUserLogin.getId().intValue());
        String encodedPassword = passwordEncoder.encode(newPassword);
        updatUserLogin.setPassword(encodedPassword);
        // updatUserLogin.setTypePassword("Permanant");
        userLoginRepo.save(updatUserLogin);

        // Save to password history
        savePasswordToHistory(updatUserLogin.getId().intValue(), encodedPassword);

    }

    public Boolean checkPasswordPolicy(String email, String password, String username) {
        validatePasswordPolicy(password, username, email, null);
        return true;
    }

    public void initiatePasswordReset(String email) {
        AppUser user = userLoginRepo.findUserByIdemail(email);
        if (user == null) {
            throw new UserNotFoundException("User doesn't exist!");
        }

        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> validResets = user.getPasswordChangeTimestamps().stream()
                .filter(ts -> ts.isAfter(now.minusHours(1)))
                .sorted()
                .collect(Collectors.toList());

        if (validResets.size() >= 3) {
            LocalDateTime oldestReset = validResets.get(0);
            LocalDateTime nextAvailable = oldestReset.plusHours(1);
            long minutesLeft = Duration.between(now, nextAvailable).toMinutes();

            throw new IllegalArgumentException(
                    "You have reached the reset limit. Please try again in " + minutesLeft + "    minute(s).");
        }

        // Generating reset token and expiry
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = now.plusMinutes(10); // 10 minutes validity

        user.setResetToken(token);
        user.setTokenExpiryTime(expiry);

        // Triggering email
        String resetUrl = resetUrlUI + token;
        // Context context = new Context();
        // context.setVariable("userName", user.getNmlogin());
        // context.setVariable("resetUrl", resetUrl);
        // String htmlContent = templateEngine.process("ForgotPassword", context);

        // SmtpDetails smtpDetails = mailService.getSmtpDetails();
        // String sender = smtpDetails.getMailId();
        // Details details = new Details(user.getNmlogin(),user.getIdemail(),
        // htmlContent, "Forgot Password", sender, null);
        // mailService.sendMailWithHtmlBody(details, smtpDetails);

        userLoginRepo.save(user);
    }

    public Boolean CheckUserActivation(String nmLogin, String loginType) {
        AppUser user = userLoginRepo.findByUsernameAndLoginType(nmLogin, loginType);
        if (user == null) {
            throw new UserNotFoundException("User doesn't exist!");
        }

        if (user.isEnabled()) {
            return true; // allow login
        } else {
            return false; // deny login
        }

    }

}
