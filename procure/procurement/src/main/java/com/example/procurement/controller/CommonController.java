package com.example.procurement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.procurement.DTO.CustomPage;
import com.example.procurement.entity.Department;
import com.example.procurement.entity.Group;
import com.example.procurement.entity.Vendor;
import com.example.procurement.service.Authservice;
import com.example.procurement.service.MasterService;
import com.example.procurement.service.VendorService;

import jakarta.validation.ConstraintViolationException;

@RestController
@RequestMapping("/api/common")
public class CommonController {

	@Autowired
	MasterService masterService;

	@Autowired
	VendorService supplierService;

	@Autowired
	Authservice authservice;

	@GetMapping("/MasterGetAll")
	private <T> CustomPage<T> toCustomPage(Page<T> page) {
		return new CustomPage<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isLast());
	}

	@PostMapping("/department/create")
	public ResponseEntity<Department> createDepartment(
			@RequestBody Department department) {

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(masterService.create(department));
	}

	/**
	 * 📋 Get All Departments
	 */
	@GetMapping("/department")
	public ResponseEntity<List<Department>> getAllDepartments() {
		return ResponseEntity.ok(masterService.getAll());
	}

	/**
	 * 🔍 Get Department By ID
	 */
	@GetMapping("/department/{id}")
	public ResponseEntity<Department> getDepartmentById(
			@PathVariable Long id) {

		return ResponseEntity.ok(masterService.getById(id));
	}

	@PostMapping("/group/create")
	public ResponseEntity<Group> createGroup(
			@RequestBody Group group) {

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(masterService.create(group));
	}

	/**
	 * 📋 Get All Groups
	 */
	@GetMapping("/group")
	public ResponseEntity<List<Group>> getAllGroups() {
		return ResponseEntity.ok(masterService.getAllGroup());
	}

	@GetMapping("/group/{id}")
	public ResponseEntity<Group> getGroupById(
			@PathVariable Long id) {

		return ResponseEntity.ok(masterService.getGroupById(id));
	}

	// // // Save unregister supplier Data
	@PostMapping("/Signup")
	public ResponseEntity<Object> saveSignupSupplierRequest(@RequestBody Vendor vendorSignup) {
		try {

			return ResponseEntity.ok(supplierService.saveSignupSupplier(vendorSignup));

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

	@PostMapping("/resetpassword")
	public ResponseEntity<Map<String, Object>> resetpassword(@RequestParam String token,
			@RequestParam String newPassword) {
		Map<String, Object> response = new HashMap<>();
		try {
			authservice.resetPasswordWithToken(token, newPassword); // Validates token & updates password
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

}
