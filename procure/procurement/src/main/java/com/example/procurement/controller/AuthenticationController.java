package com.example.procurement.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.procurement.DTO.ApiResponse;
import com.example.procurement.DTO.LoginResponse;
import com.example.procurement.DTO.UpdateUserLoginDTO;
import com.example.procurement.entity.AppUser;
import com.example.procurement.entity.Group;
import com.example.procurement.exception.InvalidcredentialException;
import com.example.procurement.exception.PasswordValidationException;
import com.example.procurement.exception.TempPasswordException;
import com.example.procurement.exception.UserNotFoundException;
import com.example.procurement.service.Authservice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    Authservice masterService;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginCheck(@RequestParam String nmLogin, @RequestParam String pwd,
            @RequestParam String loginType)
            throws Exception {
        LoginResponse returnResponse = new LoginResponse();
        try {
            // check either user enable or disable
            Boolean flag = masterService.CheckUserActivation(nmLogin, loginType);
            if (flag) {
                Map<String, String> token = masterService.authenticateUser(nmLogin, pwd, loginType);
                returnResponse.setAccesstoken(token.get("access_token"));
                returnResponse.setRefreshtoken(token.get("refresh_token"));
                returnResponse.setMessage("Successful login");
                return ResponseEntity.ok(returnResponse);
            } else {
                returnResponse.setMessage("User is not in Active state");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnResponse);
            }

        } catch (UserNotFoundException uexcp) {
            returnResponse.setMessage("User doesn't exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnResponse);
        }

        catch (InvalidcredentialException iexcp) {
            returnResponse.setMessage(iexcp.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnResponse);
        } catch (TempPasswordException iexcp) {
            returnResponse.setMessage("Temporary password used. Please change your password.");
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(returnResponse);
        } catch (RuntimeException re) {
            if (re.getMessage() != null && re.getMessage().contains("Login denied")) {
                returnResponse.setMessage(re.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(returnResponse); // 409 Conflict
            }
            // Retain original behavior for other RuntimeExceptions
            re.printStackTrace();
            returnResponse.setMessage("Internal authentication error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnResponse);
        } catch (Exception e) {
            // 👇 THIS is what was missing
            e.printStackTrace();
            returnResponse.setMessage("Internal authentication error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnResponse);
        }

    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        LoginResponse returnResponse = new LoginResponse();
        String refreshToken = request.get("refreshtoken");

        try {
            Map<String, String> token = masterService.verifyRefreshToken(refreshToken);

            returnResponse.setAccesstoken(token.get("access_token"));
            returnResponse.setRefreshtoken(token.get("refresh_token"));
            returnResponse.setMessage("Successful refreshed accesstoken");
            return ResponseEntity.ok(returnResponse);
        } catch (Exception e) {
            // Check for known exceptions from Authservice
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String username, @RequestParam String loginType) {
        masterService.logoutUser(username, loginType);
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            masterService.initiatePasswordReset(email);
            response.put("message", "Reset link sent (if account exists). Please check your email !!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestParam String token,
            @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        try {
            masterService.resetPasswordWithToken(token, newPassword); // Validates token & updates password
            response.put("message", "Password has been reset successfully.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "Failed to reset password.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Updating the Permanent Password
    @PutMapping("/update-password/{username}")
    public ResponseEntity<Map<String, Object>> changePassword(@PathVariable String username,
            @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        try {
            masterService.updatePassword(username, newPassword);
            response.put("message", "Password has been changed successfully.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "Failed to update password.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get loggedUser Data
    @GetMapping("/loggeduser/{email}")
    public ResponseEntity<Object> getloggedUserDetail(@PathVariable String email, @RequestParam String loginType) {
        return ResponseEntity.ok(masterService.getloggedUserDetail(email, loginType));
    }

    // Password Policy Validation check
    @GetMapping("/passwordpolicy/{email}")
    public ResponseEntity<Object> checkPasswordPolicy(@PathVariable String email, @RequestParam String password,
            @RequestParam String username) {
        return ResponseEntity.ok(masterService.checkPasswordPolicy(email, password, username));
    }

    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        Map<String, String> response = new HashMap<>();
        response.put("version", "1.0-DEBUG-SESSION-FIX-EPOCH");
        response.put("timestamp", ZonedDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug-session/{username}")
    public ResponseEntity<Map<String, Object>> getSessionInfo(@PathVariable String username,
            @RequestParam String loginType) {
        return ResponseEntity.ok(masterService.getSessionDebugInfo(username, loginType));
    }

    @PostMapping("/groups")
    public ResponseEntity<List<Group>> groupRoles(@RequestBody List<Group> grouproles) {
        List<Group> grouproles2 = masterService.createGroupRoles(grouproles);
        return ResponseEntity.ok(grouproles2);
    }

    @GetMapping("/groups")
    public ResponseEntity<?> groupRoles(@RequestParam(required = false) Integer id) {
        if (id != null) {
            Group grouprole = masterService.getGrouproles(id);
            return ResponseEntity.ok(grouprole);
        } else {
            List<Group> grouproles = masterService.fetchGrouproles();
            return ResponseEntity.ok(grouproles);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveUserLogin(@RequestBody @Valid AppUser userlogin)
            throws MethodArgumentNotValidException {
        try {
            AppUser userLogin = masterService.saveUserLogin(userlogin);

            String responseMessages = "User is saved with " + userLogin.getId();
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, responseMessages));

        } catch (DataIntegrityViolationException e) {
            // Handle unique constraint violation
            return handleDataIntegrityViolation(e);
        } catch (ConstraintViolationException e) {
            // Handle validation constraints (e.g., NotBlank, NotNull, @Email)
            return handleConstraintViolation(e);
        } catch (Exception e) {
            // General exception handler for other unhandled cases
            return handleGeneralException(e);
        }
    }

    @PutMapping("/user/update/{id}")
    public ResponseEntity<?> updateUserLogin(@RequestBody @Valid UpdateUserLoginDTO userlogin, @PathVariable Integer id)
            throws MethodArgumentNotValidException {
        try {
            Boolean flag = masterService.updateUserLogin(userlogin, id);
            if (flag) {
                return ResponseEntity.ok(new ApiResponse(true, "User is updated with succesfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "User is not updated, try again"));
            }
        } catch (DataIntegrityViolationException e) {
            // Handle unique constraint violation
            return handleDataIntegrityViolation(e);
        } catch (ConstraintViolationException e) {
            // Handle validation constraints (e.g., NotBlank, NotNull, @Email)
            return handleConstraintViolation(e);
        } catch (Exception e) {
            // General exception handler for other unhandled cases
            return handleGeneralException(e);
        }

    }

    // For Exception Handelling
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(error -> {
            String fieldName = error.getPropertyPath().toString();
            String message = error.getMessage();
            errors.put(fieldName, message);
            System.out.println(errors);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    private ResponseEntity<Object> handleGeneralException(Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");

        String fieldName = extractFieldNameFromException(e.getMessage());
        if (fieldName != null) {
            errorResponse.put("message", "Duplicate value in field: " + fieldName);
        } else {
            errorResponse.put("message", "Duplicate key violation");
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    private String extractFieldNameFromException(String exceptionMessage) {
        // Example of a regex pattern that can extract the field name from the error
        // message
        Pattern pattern = Pattern.compile("Key \\((.*?)\\)=\\((.*?)\\) already exists");
        Matcher matcher = pattern.matcher(exceptionMessage);
        if (matcher.find()) {
            return matcher.group(1); // group(1) captures the field name
        }
        return null; // Return null if the field name couldn't be extracted
    }

    private ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");

        e.getConstraintViolations().forEach(violation -> {
            errorResponse.put(violation.getPropertyPath().toString(), violation.getMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        Map<String, Object> errorResponse = new HashMap<>();

        // Add status to the response
        errorResponse.put("status", "error");

        // Extract the field errors and their default messages
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        // Add a generic message and the specific field errors to the response
        errorResponse.put("message", "Validation failed for the provided input.");
        errorResponse.put("errors", fieldErrors); // Field-specific errors

        // Return the map of field errors and default messages
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordValidationException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordValidationException(PasswordValidationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("valid", false);
        body.put("errors", ex.getErrors());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

}
