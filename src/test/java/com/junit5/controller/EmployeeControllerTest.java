package com.junit5.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.junit5.exception.ResourceNotFoundException;
import com.junit5.model.Employee;
import com.junit5.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

	@MockBean
	private EmployeeService employeeService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc mockMvc;
	/*
	  	The Spring MVC Test framework, also known as MockMvc, aims to provide more complete testing for Spring MVC controllers without a running server.
	  	It does that by invoking the DispatcherServlet and passing “mock” implementations of the Servlet API from the spring-test module
	  	which replicates the full Spring MVC request handling without a running server.

		MockMvc is a server side test framework that lets you verify most of the functionality of a Spring MVC 
		application using lightweight and targeted tests. You can use it on its own to perform requests and to verify responses, 
		or you can also use it through the WebTestClient API with MockMvc plugged in as the server to handle requests with.
	 */

	private Employee employee;

	@BeforeEach
	public void setUp() {
		employee = new Employee(1L, "Amar", "Patil", "amarpatil@outlook.com");
	}

	@AfterEach
	public void tearDown() {
		employee = null;
	}

	@Test
	@DisplayName("JUnit test for Create Employee")
	public void givenEmployee_whenCreatedEmployee_thenReturnEmployee() {
		try {
			given(employeeService.saveEmployee(any(Employee.class))).willReturn(employee);
			mockMvc.perform(post("/api/v1/employees").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsBytes(employee)))
					.andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
					.andExpect(jsonPath("$.lastName", is(employee.getLastName())))
					.andExpect(jsonPath("$.email", is(employee.getEmail())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("GetAllEmployees JUnit Test")
	public void givenEmployeeList_whenGetEmployees_thenReturnEmployeeList() {
		try {

			Employee e1 = new Employee(1L, "Richard", "Parker", "richard.parker@outlook.com");
			Employee e2 = new Employee(2L, "Peter", "Parker", "peter.parker@outlook.com");
			List<Employee> employeeList = List.of(e1, e2);

			given(employeeService.getAllEmployees()).willReturn(employeeList);

			mockMvc.perform(get("/api/v1/employees")).andExpect(status().isOk())
					.andExpect(jsonPath("$.size()", is(employeeList.size())));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("Get Employee By Employee Id")
	public void shouldGetEmployeeById() {
		given(employeeService.getEmployeeById(employee.getId())).willReturn(employee);

		try {
			mockMvc.perform(get("/api/v1/employees/{id}", employee.getId())).andExpect(status().isOk())
					.andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
					.andExpect(jsonPath("$.lastName", is(employee.getLastName())))
					.andExpect(jsonPath("$.email", is(employee.getEmail())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	@DisplayName("JUnit Test for getEmployeeById Operation - ResourceNotFoundException")
	public void givenInvalidEmployeeId_whenGetEmployeeById_thenThrowResourseNotFoundException() throws Exception {
		given(employeeService.getEmployeeById(employee.getId())).willThrow(ResourceNotFoundException.class);

		mockMvc.perform(get("/api/v1/employees/{id}", employee.getId())).andExpect(status().isNotFound());

	}

	@Test
	@DisplayName("JUnit Test for Update Employee")
	public void shouldUpdateEmployee() throws JsonProcessingException, Exception {

		// given - precondition or setup
		Employee employeeForUpdate = new Employee(employee.getId(), "Peter", "Parker", "peter.parker@outlook.com");
		given(employeeService.updateEmployee(anyLong(), any(Employee.class))).willReturn(employeeForUpdate);

		// when - action or the behaviour
		ResultActions response = mockMvc.perform(put("/api/v1/employees/{id}", employee.getId())
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(employeeForUpdate)));
		// then - verify the output
		response.andExpect(status().isOk()).andExpect(jsonPath("$.firstName", is(employeeForUpdate.getFirstName())))
				.andExpect(jsonPath("$.lastName", is(employeeForUpdate.getLastName())))
				.andExpect(jsonPath("$.email", is(employeeForUpdate.getEmail())));
	}

	@Test
	@DisplayName("JUnit test for updateEmployee operation - ResourceNotFoundException")
	public void givenInvalidEmployeeWithUpdates_whenUpdateEmployee_thenThrowResourseNotFoundException()
			throws JsonProcessingException, Exception {
		// given - precondition
		Employee employeeForUpdate = new Employee(employee.getId(), "Peter", "Parker", "peter.parker@outlook.com");
		given(employeeService.updateEmployee(anyLong(), any(Employee.class)))
				.willThrow(ResourceNotFoundException.class);

		mockMvc.perform(put("/api/v1/employees/{id}", employee.getId()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(employeeForUpdate))).andExpect(status().isNotFound());

	}

	@Test
	@DisplayName("JUnit test for Deleting employee by id")
	public void givenEmployeeId_whenDeleteEmployeeById_thenReturnTrue() throws Exception {
		// given

		willDoNothing().given(employeeService).deleteEmployeeById(employee.getId());

		mockMvc.perform(delete("/api/v1/employees/{id}", employee.getId())).andExpect(status().isOk());
	}

	@Test
	@DisplayName("JUnit test for delete employee operation - ResourceNotFoundException")
	public void givenInvalidEmployeeId_whenDeleteEmployeeById_thenThrowResourseNotFoundException() throws Exception {
		// given
		willThrow(ResourceNotFoundException.class).given(employeeService).deleteEmployeeById(employee.getId());

		mockMvc.perform(delete("/api/v1/employees/{id}", employee.getId())).andExpect(status().isNotFound());

	}

}
