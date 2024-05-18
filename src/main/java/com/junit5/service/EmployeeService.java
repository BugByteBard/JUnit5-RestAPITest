package com.junit5.service;


import java.util.List;

import com.junit5.model.Employee;

public interface EmployeeService {
	public Employee saveEmployee(Employee employee);
	public List<Employee> getAllEmployees();
	public Employee getEmployeeById(Long id);
	public Employee updateEmployee(Long employeeId, Employee employee);
	public void deleteEmployeeById(Long id);
}
